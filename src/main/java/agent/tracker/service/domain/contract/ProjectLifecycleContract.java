package agent.tracker.service.domain.contract;

import agent.tracker.service.domain.model.Project;
import java.util.List;
import java.util.Optional;

public interface ProjectLifecycleContract {

    Project createProject(CreateProjectCommand command);

    Project updateProject(UpdateProjectCommand command);

    Optional<Project> getProjectById(String projectId);

    List<Project> listProjects();
}
