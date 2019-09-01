package nh.graphql.projectmgmt.graphql.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;

import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.servlet.context.GraphQLContext;
import graphql.servlet.context.GraphQLContextBuilder;
import nh.graphql.projectmgmt.domain.ProjectRepository;
import nh.graphql.projectmgmt.domain.TaskPublisher;
import nh.graphql.projectmgmt.domain.TaskRepository;
import nh.graphql.projectmgmt.domain.TaskService;
import nh.graphql.projectmgmt.domain.user.UserService;
import nh.graphql.projectmgmt.graphql.fetcher.ProjectDataLoaders;

@Component
public class ProjectGraphQLContextBuilder implements GraphQLContextBuilder {

  @Autowired
  private UserService userService;
  @Autowired
  private ProjectRepository projectRepository;
  @Autowired
  private TaskService taskService;
  @Autowired
  private TaskRepository taskRepository;
  @Autowired
  private TaskPublisher taskPublisher;

  private ProjectDataLoaders projectDataLoaders = new ProjectDataLoaders();

  private InteralProjectMgmtGraphQLContext interalProjectMgmtGraphQLContext = new InteralProjectMgmtGraphQLContext();

  @Override
  public GraphQLContext build(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    GraphQLContext graphQLContext = new ProjectMgmtGraphQLServletContext(httpServletRequest, httpServletResponse,
        interalProjectMgmtGraphQLContext);
    addDataLoaders(graphQLContext);
    return graphQLContext;
  }

  @Override
  public GraphQLContext build(Session session, HandshakeRequest handshakeRequest) {
    GraphQLContext context = new ProjectMgmtGraphQLWebSocketContext(session, handshakeRequest,
        interalProjectMgmtGraphQLContext);
    addDataLoaders(context);
    return context;
  }

  @Override
  public GraphQLContext build() {
    GraphQLContext context = new ProjectMgmtGraphQLDefaultContext(interalProjectMgmtGraphQLContext);
    addDataLoaders(context);
    return context;
  }

  private void addDataLoaders(GraphQLContext context) {
    DataLoaderRegistry dataLoaderRegistry = context.getDataLoaderRegistry().orElseThrow();
    dataLoaderRegistry.register("userDataLoader", DataLoader.newDataLoader(projectDataLoaders.userBatchLoader));
  }

  class InteralProjectMgmtGraphQLContext implements ProjectMgmtGraphQLContext {
    @Override
    public UserService getUserService() {
      return userService;
    }

    @Override
    public ProjectRepository getProjectRepository() {
      return projectRepository;
    }

    @Override
    public TaskService getTaskService() {
      return taskService;
    }

    @Override
    public TaskRepository getTaskRepository() {
      return taskRepository;
    }

    @Override
    public TaskPublisher getTaskPublisher() {
      return taskPublisher;
    }

  }

}