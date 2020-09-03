/*
 * insert_citation-source-panel-list.tsx
 *
 * Copyright (C) 2020 by RStudio, PBC
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
import React from "react";

import { FixedSizeList } from "react-window";

import { EditorUI } from "../../../api/ui";
import { BibliographySource } from "../../../api/bibliography/bibliography";
import { WidgetProps } from "../../../api/widgets/react";

import { CitationSourcePanelListItem } from "./insert_citation-source-panel-list-item";

import './insert_citation-source-panel-list.css';

export interface CitationSourceListProps extends WidgetProps {
  height: number;
  itemData: BibliographySource[];
  sourcesToAdd: BibliographySource[];
  noResultsText: string;
  addSource: (source: BibliographySource) => void;
  removeSource: (source: BibliographySource) => void;
  confirm: VoidFunction;
  ui: EditorUI;
}

export const CitationSourceList = React.forwardRef<HTMLDivElement, CitationSourceListProps>((props: CitationSourceListProps, ref) => {
  const [selectedIndex, setSelectedIndex] = React.useState<number>(0);
  const [focused, setFocused] = React.useState<boolean>(false);
  const fixedList = React.useRef<FixedSizeList>(null);

  // Whenever selection changed, ensure that we are scrolled to that item
  React.useLayoutEffect(() => {
    fixedList.current?.scrollToItem(selectedIndex);
  }, [selectedIndex]);

  // Reset the index whenever the data changes
  React.useEffect(() => {
    setSelectedIndex(0);
  }, [props.itemData]);

  // Item height and consequently page height
  const itemHeight = 64;
  const itemsPerPage = Math.floor(props.height / itemHeight);

  // Upddate selected item index (this will manage bounds)
  const incrementIndex = (event: React.KeyboardEvent, index: number) => {
    event.stopPropagation();
    event.preventDefault();
    const maxIndex = props.itemData.length - 1;
    setSelectedIndex(Math.min(Math.max(0, index), maxIndex));
  };

  // Toggle the currently selected item as added or removed
  const toggleSelectedSource = (event: React.KeyboardEvent) => {
    event.stopPropagation();
    event.preventDefault();

    const source = props.itemData[selectedIndex];
    if (source) {
      if (props.sourcesToAdd.includes(source)) {
        props.removeSource(source);
      } else {
        props.addSource(source);
      }
    }
  };

  const handleListKeyDown = (event: React.KeyboardEvent) => {
    switch (event.key) {
      case 'ArrowUp':
        incrementIndex(event, selectedIndex - 1);
        break;

      case 'ArrowDown':
        incrementIndex(event, selectedIndex + 1);
        break;

      case 'PageDown':
        incrementIndex(event, selectedIndex + itemsPerPage);
        break;

      case 'PageUp':
        incrementIndex(event, selectedIndex - itemsPerPage);
        break;

      case 'Enter':
        toggleSelectedSource(event);
        props.confirm();
        break;
      case ' ':
        toggleSelectedSource(event);
        break;
    }
  };

  // Focus / Blur are used to track whether to show selection highlighting
  const onFocus = (event: React.FocusEvent<HTMLDivElement>) => {
    setFocused(true);
  };

  const onBlur = (event: React.FocusEvent<HTMLDivElement>) => {
    setFocused(false);
  };



  const classes = ['pm-insert-citation-source-panel-list-container'].concat(props.classes || []).join(' ');

  return (
    props.itemData.length === 0 ?
      (<div className={classes} style={{ height: props.height + 'px' }} ref={ref} >
        <div className='pm-insert-citation-source-panel-list-noresults-text'>{props.noResultsText}</div>
      </div >) :
      (
        <div tabIndex={0} onKeyDown={handleListKeyDown} onFocus={onFocus} onBlur={onBlur} ref={ref} className={classes}>
          <FixedSizeList
            height={props.height}
            width='100%'
            itemCount={props.itemData.length}
            itemSize={itemHeight}
            itemData={{
              selectedIndex,
              allSources: props.itemData,
              sourcesToAdd: props.sourcesToAdd,
              addSource: props.addSource,
              removeSource: props.removeSource,
              ui: props.ui,
              showSeparator: true,
              showSelection: focused,
              preventFocus: true
            }}
            ref={fixedList}
          >
            {CitationSourcePanelListItem}
          </FixedSizeList>
        </div>
      )
  );
});



