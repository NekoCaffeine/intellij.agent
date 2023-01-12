# intellij.agent
Disable plugin compatibility checks and make IDEA support run on OpenJDK 17.
Force internal actions to be enabled.

```
IntelliJ IDEA with Maho 2022.3.2 Preview (Community Edition)
Build #IC-223.8617.9, built on January 11, 2023
```

Attach to the idea as a javaagent, using an absolute path.

Please download and unzip from release.

Add the following JVM parameters:
`-javaagent:<dir>/intellij.agent.jar`

If you want to change the IntelliJ startup logo, add the following parameters:
`-Dintellij.logoOverride=<Image Path>`

If you want to change the IntelliJ window icon, add the following parameters:
`-Dintellij.iconOverride=<Image Path>`
