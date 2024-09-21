# intellij.agent
Disable plugin compatibility checks and make IDEA support run on OpenJDK 21.
Force internal actions to be enabled.

```
IntelliJ IDEA with Maho 2024.3 EAP (Community Edition)
Build #IC-243.15521.24, built on September 19, 2024
```

Attach to the idea as a javaagent, using an absolute path.

Please download and unzip from release.

Add the following JVM parameters:
`-javaagent:<dir>/intellij.agent.jar`

If you want to change the IntelliJ startup logo, add the following parameters:
`-Dintellij.logoOverride=<Image Path>`

If you want to change the IntelliJ window icon, add the following parameters:
`-Dintellij.iconOverride=<Image Path>`
