module ro.nq.metaexplorer {
    requires java.sql;

    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.swing;

    requires org.apache.pdfbox;

    opens ro.nq.metaexplorer.Applications to javafx.graphics, javafx.fxml;
    opens ro.nq.metaexplorer.AutonomousViews to javafx.fxml;
    opens ro.nq.metaexplorer.BorderControllers to javafx.fxml;
    opens ro.nq.metaexplorer.DataEncapsulations to javafx.fxml;
    opens ro.nq.metaexplorer.Dialogs to javafx.fxml;
    opens ro.nq.metaexplorer.ItemViews to javafx.fxml;
    opens ro.nq.metaexplorer.SubControllers to javafx.fxml;
    opens ro.nq.metaexplorer.Utilities to javafx.fxml;
}