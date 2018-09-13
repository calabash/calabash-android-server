package sh.calaba.instrumentationbackend.query.ast;

import java.util.List;

import sh.calaba.instrumentationbackend.query.ui.UIObject;

public interface UIQueryAST {
	public List<UIObject> evaluateWithViews(List<? extends UIObject> inputUIObjects,
											UIQueryDirection direction, UIQueryVisibility visibility);
}
