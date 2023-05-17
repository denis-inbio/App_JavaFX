package ro.nq.metaexplorer.DataEncapsulations;

public class ClassConstants {
    public final Class _class;
    public final String debugClassName;
    public final String fxmlPathName;
    public final String stylesheetPathName;
    public final String bundlePathName;
    public ClassConstants(Class _class, String debugClassName, String fxmlPathName, String stylesheetPathName, String bundlePathName) {
        this._class = _class;
        this.debugClassName = debugClassName;
        this.fxmlPathName = fxmlPathName;
        this.stylesheetPathName = stylesheetPathName;
        this.bundlePathName = bundlePathName;
    }
}
