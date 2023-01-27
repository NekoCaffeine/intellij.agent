# intellij.agent
Disable plugin compatibility checks and make IDEA support run on OpenJDK 17.
Force internal actions to be enabled.

```
IntelliJ IDEA with Maho 2023.1 EAP (Community Edition)
Build #IC-231.5920.14, built on January 27, 2023
```

Attach to the idea as a javaagent, using an absolute path.

Please download and unzip from release.

Add the following JVM parameters:
`-javaagent:<dir>/intellij.agent.jar`

If you want to change the IntelliJ startup logo, add the following parameters:
`-Dintellij.logoOverride=<Image Path>`

If you want to change the IntelliJ window icon, add the following parameters:
`-Dintellij.iconOverride=<Image Path>`
