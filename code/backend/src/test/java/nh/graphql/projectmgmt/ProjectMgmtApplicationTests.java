package nh.graphql.projectmgmt;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ProjectMgmtApplicationTests {

  private static final Logger logger = LoggerFactory.getLogger(ProjectMgmtApplicationTests.class);

  @Autowired
  TestRestTemplate restTemplate;

  @Autowired
  Importer importer;

  ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void ping() throws JsonProcessingException {
    GraphQLTestResponse graphQLTestResponse = execute("query {ping}");
    assertThat(graphQLTestResponse.get("$.data.ping")).startsWith("Hello, World");
  }

  @Test
  public void usersQuery() {
    String query = loadGraphQL("UsersQuery");
    GraphQLTestResponse graphQLTestResponse = execute(query);
    assertThat(graphQLTestResponse.getInt("$.data.users.length()")).isEqualTo(8);
  }

  @Test
  public void addTask() {
      String mutation = loadGraphQL("addTaskMutation");
      GraphQLTestResponse graphQLTestResponse = execute(mutation);
      assertThat(graphQLTestResponse.get("$.data.addTask.title")).isEqualTo("New Task");
      assertThat(graphQLTestResponse.get("$.data.addTask.description")).isEqualTo("New Description");
      assertThat(graphQLTestResponse.get("$.data.addTask.toBeFinishedAt")).isNotBlank();
  }

  protected String loadGraphQL(String name) {
    String resourceName = name + ".graphql";

    try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
      return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new IllegalStateException("Could not load resource '" + resourceName + "' from classpath: " + ex, ex);
    }

  }

  protected GraphQLTestResponse execute(String graphqlQuery) {
    String jsonRequest = createJsonQuery(graphqlQuery);
    HttpEntity<Object> httpEntity = forJson(jsonRequest);

    ResponseEntity<String> response = restTemplate.exchange("/graphql", HttpMethod.POST, httpEntity, String.class);
    GraphQLTestResponse graphQLTestResponse = new GraphQLTestResponse(response.getStatusCodeValue(),
        response.getBody());
    logger.info("GRAPHQL RESPONSE ====>>> {}", graphQLTestResponse);

    return graphQLTestResponse;
  }

  // ALL FROM
  // https://github.com/graphql-java-kickstart/graphql-spring-boot/blob/master/graphql-spring-boot-test/src/main/java/com/graphql/spring/boot/test
  private String createJsonQuery(String graphql) {

    ObjectNode wrapper = objectMapper.createObjectNode();
    wrapper.put("query", graphql);
//  wrapper.set("variables", variables);
    try {
      return objectMapper.writeValueAsString(wrapper);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Could not create json: " + e, e);
    }
  }

  static HttpEntity<Object> forJson(String json) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
    return new HttpEntity<>(json, headers);
  }

}
