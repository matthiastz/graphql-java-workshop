package nh.graphql.tasks.graphql;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

/**
 * @author Nils Hartmann (nils@nilshartmann.net)
 */

@Component
public class GraphQLApi {

  private final static Logger logger = LoggerFactory.getLogger(GraphQLApi.class);

  @Autowired
  private QueryDataFetchers queryDataFetchers;
  @Autowired
  private ProjectDataFetchers projectDataFetchers;
  @Autowired
  private TaskFetchers taskFetchers;
  @Autowired
  private MutationFetchers mutationFetchers;

  @Bean
  public GraphQLSchema graphQLSchema() {
    logger.info("Building GraphQL Schema");

    SchemaParser schemaParser = new SchemaParser();
    InputStream inputStream = getClass().getResourceAsStream("/tasks.graphqls");
    TypeDefinitionRegistry typeRegistry = schemaParser.parse(new InputStreamReader(inputStream));

    RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring() //
        .type(newTypeWiring("Query") //
            .dataFetcher("users", queryDataFetchers.users) //
            .dataFetcher("user", queryDataFetchers.user) //
            .dataFetcher("projects", queryDataFetchers.projects) //
            .dataFetcher("project", queryDataFetchers.project)) //
        .type(newTypeWiring("Mutation") //
            .dataFetcher("updateTaskState", mutationFetchers.updateTaskState) //
            .dataFetcher("addTask", mutationFetchers.addTask))
        .type(newTypeWiring("Project") //
            .dataFetcher("task", projectDataFetchers.task) //
            .dataFetcher("owner", projectDataFetchers.owner))
        .type(newTypeWiring("Task") //
            .dataFetcher("assignee", taskFetchers.assignee)) //
        .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
  }

  @Bean
  public GraphQL graphql(GraphQLSchema graphQLSchema) {
    return GraphQL.newGraphQL(graphQLSchema).build();
  }

}