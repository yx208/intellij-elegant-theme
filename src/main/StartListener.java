package main;

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

public class StartListener implements ProjectManagerListener {

    @Override
    public void projectOpened(@NotNull Project project) {

        Notification notification = new Notification("Print", "Hello", NotificationType.INFORMATION);
        // 在提示消息中，增加一个 Action，可以通过 Action 一步打开配置界面
        Notifications.Bus.notify(notification, project);

    }
}