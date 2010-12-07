/*
 * RClientMetrics.cpp
 *
 * Copyright (C) 2009-11 by RStudio, Inc.
 *
 * This program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */

#include "RClientMetrics.hpp"

#include <iostream>

#include <core/Settings.hpp>

#include <r/ROptions.hpp>
#include <r/session/RSession.hpp>

#include "graphics/RGraphicsDevice.hpp"

using namespace core ;

namespace r {
namespace session {
namespace client_metrics {

namespace {   
const char * const kConsoleWidth = "r.session.client_metrics.console-width";
const char * const kGraphicsWidth = "r.session.client_metrics.graphics-width";
const char * const kGraphicsHeight = "r.session.client_metrics.graphics-height";
   
std::ostream& operator << (std::ostream& os, const RClientMetrics& m)
{
   os << "console: " << m.consoleWidth << " "
      << "graphics: " << m.graphicsWidth << "," << m.graphicsHeight ;
   
   return os;
}
   
}   
   
RClientMetrics get()
{
   RClientMetrics metrics ;
   metrics.consoleWidth = r::options::getOptionWidth();
   metrics.graphicsWidth = graphics::device::getWidth();
   metrics.graphicsHeight = graphics::device::getHeight();
   return metrics;
}
   
void set(const RClientMetrics& metrics)
{
   // set console width
   r::options::setOptionWidth(metrics.consoleWidth);
   
   // set graphics size, however don't do anything if width or height is less
   // than or equal to 0) 
   // (means the graphics window is minimized)
   if (metrics.graphicsWidth > 0 && metrics.graphicsHeight > 0)
   {
      // enforce a minimum graphics size so we don't get display 
      // list redraw errors -- note that setting the device to a size 
      // which diverges from the actual client size will break locator
      // so we need to set the size small enough that there is no way 
      // it can reasonably be used for locator
      int width = std::max(metrics.graphicsWidth, 150);
      int height = std::max(metrics.graphicsHeight, 150);
      graphics::device::setSize(width, height);
   }
}
      
void save(Settings* pSettings)
{
   // get the client metrics
   RClientMetrics metrics = client_metrics::get();
   
   // save them
   pSettings->beginUpdate();
   pSettings->set(kConsoleWidth, metrics.consoleWidth);
   pSettings->set(kGraphicsWidth, metrics.graphicsWidth);
   pSettings->set(kGraphicsHeight, metrics.graphicsHeight);
   pSettings->endUpdate();
}
   
void restore(const Settings& settings)
{
   // read the client metrics (specify defaults to be defensive)
   RClientMetrics metrics ;
   metrics.consoleWidth = settings.getInt(kConsoleWidth, 
                                          r::options::kDefaultWidth);
   
   metrics.graphicsWidth = settings.getInt(kGraphicsWidth,
                                           graphics::device::kDefaultWidth);
   
   metrics.graphicsHeight = settings.getInt(kGraphicsHeight,
                                            graphics::device::kDefaultHeight);
   
   // set them
   set(metrics);
}
   

} // namespace client_metrics
} // namespace session
} // namespace r



