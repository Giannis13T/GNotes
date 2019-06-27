
module application {
	opens application to javafx.fxml;
	exports application;

	requires transitive java.sql;
	requires transitive javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires transitive javafx.graphics;
	requires javafx.web;
	/*requires java.logging;
	requires java.management;
	requires java.naming;
	requires java.transaction.xa;
	requires java.xml;*/
}