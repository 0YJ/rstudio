/*
 * DesktopInfo.cpp
 *
 * Copyright (C) 2009-17 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */

#include "DesktopInfo.hpp"

#include <atomic>
#include <set>

#include <boost/algorithm/string.hpp>

#include <QThread>

#include <core/Algorithm.hpp>
#include <core/SafeConvert.hpp>
#include <core/system/Process.hpp>
#include <core/system/Environment.hpp>
#include <core/system/Process.hpp>
#include <core/FileSerializer.hpp>

#include "DesktopOptions.hpp"
#include "DesktopSynctex.hpp"

#define kLsbRelease    "/etc/lsb-release"
#define kRedhatRelease "/etc/redhat-release"
#define kOsRelease     "/etc/os-release"

#define kUnknown QStringLiteral("(unknown)")

using namespace rstudio::core;

namespace rstudio {
namespace desktop {

namespace {

#ifndef Q_OS_MAC
std::atomic<bool> s_abortRequested(false);
QThread* s_fontDatabaseWorker = nullptr;
#endif

QString s_platform             = kUnknown;
QString s_version              = kUnknown;
QString s_sumatraPdfExePath    = kUnknown;
QString s_fixedWidthFontList   = QStringLiteral("");
int     s_chromiumDevtoolsPort = -1;
double  s_zoomLevel            = 1.0;

#ifndef Q_OS_MAC

void buildFontDatabaseImpl()
{
   QFontDatabase db;

   QStringList fontList;
   for (const QString& family : db.families())
   {
      if (s_abortRequested)
         return;

#ifdef _WIN32
      // screen out annoying Qt warnings when attempting to
      // initialize incompatible fonts
      static std::set<std::string> blacklist = {
         "8514oem",
         "Fixedsys",
         "Modern",
         "MS Sans Serif",
         "MS Serif",
         "Roman",
         "Script",
         "Small Fonts",
         "System",
         "Terminal"
      };

      if (blacklist.count(family.toStdString()))
         continue;
#endif

      if (isFixedWidthFont(QFont(family, 12)))
         fontList.append(family);
   }

   QString fonts = fontList.join(QStringLiteral("\n"));

   // NOTE: invokeMethod() is used to ensure thread-safe communication
   QMetaObject::invokeMethod(
            &desktopInfo(),
            "setFixedWidthFontList",
            Q_ARG(QString, fonts));
}

void buildFontDatabase()
{
#ifdef Q_OS_LINUX
   // if fontconfig is installed, we can use it to query monospace
   // fonts using its own cache (and it should be much more performant
   // than asking Qt to build the font database on demand)
   core::system::ProcessOptions options;
   core::system::ProcessResult result;
   Error error = core::system::runCommand("fc-list :spacing=100 -f '%{fullname}\n' | sort | uniq", options, &result);
   if (!error)
   {
      std::vector<std::string> fonts;
      for (const std::string& line : core::algorithm::split(result.stdOut, "\n"))
      {
         if (line.empty())
            continue;

         // if a font has multiple names for different languages (e.g. some Japanese fonts
         // have names in both English + Japanese) then just take the first reported name
         auto idx = line.find(',');
         auto font = (idx == std::string::npos) ? line : line.substr(0, idx);

         // remove a trailing 'Regular'
         if (boost::algorithm::ends_with(font, " Regular"))
            font = font.substr(0, font.length() - strlen(" Regular"));

         // TODO: right now, we just use the full font name as-is; we should instead
         // collect fonts alongside their supported styles and apply those separately
         // in the front-end (e.g. setting all of 'font', 'font-style' and 'font-weight'
         // as appropriate for the selected font). until then, screen out font variants
         // with explicit weighting / styling
         if (boost::algorithm::ends_with(font, " Bold") ||
             boost::algorithm::ends_with(font, " Oblique") ||
             boost::algorithm::ends_with(font, " Italic"))
         {
            continue;
         }

         // add the font
         fonts.push_back(font);
      }

      QString fontList = QString::fromStdString(core::algorithm::join(fonts, "\n"));
      s_fixedWidthFontList = fontList;
      return;
   }

#endif

   s_fontDatabaseWorker = QThread::create(buildFontDatabaseImpl);
   s_fontDatabaseWorker->start();
}

#else

void buildFontDatabase()
{
   s_fixedWidthFontList = desktop::getFixedWidthFontList();
}

#endif

#ifdef Q_OS_LINUX

void readEntry(
      const std::map<std::string, std::string>& entries,
      const char* key,
      QString* pOutput)
{
   if (entries.count(key))
   {
      *pOutput = QString::fromStdString(entries.at(key)).toLower();
   }
}

void initializeLsbRelease()
{
   std::map<std::string, std::string> entries;
   Error error = core::readStringMapFromFile(FilePath(kLsbRelease), &entries);

   if (error)
      LOG_ERROR(error);

   readEntry(entries, "DISTRIB_ID", &s_platform);
   readEntry(entries, "DISTRIB_RELEASE", &s_version);
}

void initializeRedhatRelease()
{
   std::string contents;
   Error error = core::readStringFromFile(
            FilePath(kRedhatRelease),
            &contents);
   if (error)
      LOG_ERROR(error);

   if (contents.find("CentOS") != std::string::npos)
      s_platform = QStringLiteral("centos");
   else if (contents.find("Red Hat Enterprise Linux"))
      s_platform = QStringLiteral("rhel");
}

void initializeOsRelease()
{
   std::map<std::string, std::string> entries;
   Error error = core::readStringMapFromFile(
            FilePath(kOsRelease),
            &entries);

   if (error)
      LOG_ERROR(error);

   readEntry(entries, "ID", &s_platform);
   readEntry(entries, "VERSION_ID", &s_version);
}

#endif /* Q_OS_LINUX */

void initialize()
{
   buildFontDatabase();

#ifdef Q_OS_LINUX
   if (FilePath(kOsRelease).exists())
   {
      initializeOsRelease();
      return;
   }
   
   if (FilePath(kLsbRelease).exists())
   {
      initializeLsbRelease();
      return;
   }

   if (FilePath(kRedhatRelease).exists())
   {
      initializeRedhatRelease();
      return;
   }
#endif
}

} // end anonymous namespace

void DesktopInfo::onClose()
{
#ifndef Q_OS_MAC
   if (s_fontDatabaseWorker && s_fontDatabaseWorker->isRunning())
   {
      s_abortRequested = true;
      s_fontDatabaseWorker->wait(1000);
   }
#endif
}

DesktopInfo::DesktopInfo(QObject* parent)
   : QObject(parent)
{
   initialize();
}

QString DesktopInfo::getPlatform()
{
   return s_platform;
}

QString DesktopInfo::getVersion()
{
   return s_version;
}

bool DesktopInfo::desktopHooksAvailable()
{
   return true;
}

QString DesktopInfo::getScrollingCompensationType()
{
#if defined(Q_OS_WIN32)
   return QStringLiteral("Win");
#elif defined(Q_OS_MAC)
   return QStringLiteral("Mac");
#else
   return QStringLiteral("None");
#endif
}

QString DesktopInfo::getFixedWidthFontList()
{
   return s_fixedWidthFontList;
}

void DesktopInfo::setFixedWidthFontList(QString fontList)
{
   if (s_fixedWidthFontList != fontList)
   {
      s_fixedWidthFontList = fontList;
      emit fixedWidthFontListChanged(fontList);
   }
}

QString DesktopInfo::getFixedWidthFont()
{
   return options().fixedWidthFont();
}

void DesktopInfo::setFixedWidthFont(QString font)
{
   if (font != options().fixedWidthFont())
   {
      options().setFixedWidthFont(font);
      emit fixedWidthFontChanged(font);
   }
}

QString DesktopInfo::getProportionalFont()
{
   return options().proportionalFont();
}

void DesktopInfo::setProportionalFont(QString font)
{
   if (font != options().proportionalFont())
   {
      options().setProportionalFont(font);
      emit proportionalFontChanged(font);
   }
}

QString DesktopInfo::getDesktopSynctexViewer()
{
   return Synctex::desktopViewerInfo().name;
}

void DesktopInfo::setDesktopSynctexViewer(QString)
{
   qWarning() << "setDesktopSynctexViewer() not implemented";
}

QString DesktopInfo::getSumatraPdfExePath()
{
   return s_sumatraPdfExePath;
}

void DesktopInfo::setSumatraPdfExePath(QString path)
{
   if (s_sumatraPdfExePath != path)
   {
      s_sumatraPdfExePath = path;
      emit sumatraPdfExePathChanged(path);
   }
}

double DesktopInfo::getZoomLevel()
{
   return s_zoomLevel;
}

void DesktopInfo::setZoomLevel(double zoomLevel)
{
   if (zoomLevel != s_zoomLevel)
   {
      s_zoomLevel = zoomLevel;
      emit zoomLevelChanged(zoomLevel);
   }
}

int DesktopInfo::getChromiumDevtoolsPort()
{
   return s_chromiumDevtoolsPort;
}

void DesktopInfo::setChromiumDevtoolsPort(int port)
{
   if (s_chromiumDevtoolsPort != port)
   {
      s_chromiumDevtoolsPort = port;
      core::system::setenv("QT_WEBENGINE_REMOTE_DEBUGGING", safe_convert::numberToString(port));
      emit chromiumDevtoolsPortChanged(port);
   }
}

} // end namespace desktop
} // end namespace rstudio
