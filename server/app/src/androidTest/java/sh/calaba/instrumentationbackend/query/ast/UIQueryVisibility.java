package sh.calaba.instrumentationbackend.query.ast;

import java.util.ArrayList;
import java.util.List;

import sh.calaba.instrumentationbackend.query.ui.UIObject;
import sh.calaba.instrumentationbackend.query.ui.UIObjectView;

public enum UIQueryVisibility implements UIQueryAST {
	ALL {
		@Override
		public List<UIObject> evaluateWithViews(List<? extends UIObject> inputUIObjects,
															 UIQueryDirection direction, UIQueryVisibility self) {
			return new ArrayList<UIObject>(inputUIObjects);
		}
	},
	
	
	VISIBLE {
		@Override
		public List<UIObject> evaluateWithViews(List<? extends UIObject> inputUIObjects,
															 UIQueryDirection direction, UIQueryVisibility self) {
			List<UIObject> filtered = new ArrayList<UIObject>(inputUIObjects.size());

			for (UIObject uiObject : UIQueryUtils.uniq(inputUIObjects)) {
				if (UIQueryUtils.isVisible(uiObject)) {
				    filtered.add(uiObject);
				}				
			}			
			return filtered;
		} 
	};
	
}
