package ge.freeuni.informatics.acceptance;

import ge.freeuni.informatics.acceptance.base.BaseAcceptanceTest;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.model.post.PostComment;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.repository.contestroom.ContestRoomJpaRepository;
import ge.freeuni.informatics.repository.post.CommentJpaRepository;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class PermissionAcceptanceTest extends BaseAcceptanceTest {

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Autowired
    private ContestRoomJpaRepository contestRoomJpaRepository;

    @Autowired
    private CommentJpaRepository commentJpaRepository;

    private static final Path PERMISSIONS_FILE_PATH = Path.of(
            "src/test/java/ge/freeuni/informatics/acceptance/permissions.md"
    );
    private static final Pattern PERMISSION_LINE_PATTERN = Pattern.compile(
            "^(\\w+)\\s*\\|\\s*(GET|POST|PUT|PATCH|DELETE)\\s+([^\\s]+)\\s*->\\s*([A-Za-z0-9_$.]+#[A-Za-z0-9_]+)\\s*$"
    );
    private static final Set<String> VALID_ROLES = Set.of("NONE", "STUDENT", "TEACHER", "ADMIN", "WORKER");
    private static final String PASSWORD = "password123";

    private User teacher;
    private User admin;
    private User memberStudent;
    private User nonMemberStudent;
    private ContestRoom room;
    private Contest contest;
    private Task task;
    private Long postId;
    private Long commentId;
    private Long submissionId;

    @BeforeEach
    void setUpScenario() {
        teacher = testDataFactory.createTeacher("permTeacher", PASSWORD);
        admin = testDataFactory.createAdmin("permAdmin", PASSWORD);
        memberStudent = testDataFactory.createStudent("permMemberStudent", PASSWORD);
        nonMemberStudent = testDataFactory.createStudent("permNonMemberStudent", PASSWORD);

        room = testDataFactory.createContestRoom("Permission Room", teacher, List.of(memberStudent));
        room.setOpen(false);
        room = contestRoomJpaRepository.save(room);

        contest = testDataFactory.createLiveContest("Permission Contest", room, 30);
        task = testDataFactory.createTask(contest, "A", "Permission Task", 2);
        testDataFactory.registerUserForContest(contest, memberStudent);

        postId = createDraftPostAsTeacher();
        commentId = createCommentAsMember(postId);
        submissionId = 1L;
    }

    @Test
    @DisplayName("permissions.md must cover every discovered /api endpoint")
    void shouldMatchPermissionsFileWithDiscoveredEndpoints() throws IOException {
        Set<EndpointSignature> discovered = discoverApiEndpoints();
        Set<PermissionEntry> documentedEntries = parsePermissionsFile();
        Set<EndpointSignature> documented = documentedEntries.stream()
                .map(PermissionEntry::endpoint)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<EndpointSignature> missingInPermissions = new LinkedHashSet<>(discovered);
        missingInPermissions.removeAll(documented);

        Set<EndpointSignature> staleInPermissions = new LinkedHashSet<>(documented);
        staleInPermissions.removeAll(discovered);

        assertThat(discovered)
                .as("No /api endpoints were discovered. Check Spring mapping setup.")
                .isNotEmpty();

        assertThat(missingInPermissions)
                .as("Endpoints exist in code but are missing in permissions.md:\n%s", joinSignatures(missingInPermissions))
                .isEmpty();

        assertThat(staleInPermissions)
                .as("Entries exist in permissions.md but are not mapped in code:\n%s", joinSignatures(staleInPermissions))
                .isEmpty();
    }

    @Test
    @DisplayName("disallowed actors must be denied for each documented endpoint")
    void shouldDenyDisallowedActorsOnEachDocumentedEndpoint() throws IOException {
        Set<PermissionEntry> entries = parsePermissionsFile();
        List<String> failures = new ArrayList<>();

        for (PermissionEntry entry : entries) {
            for (Actor actor : Actor.values()) {
                if (!shouldBeDenied(entry, actor)) {
                    continue;
                }

                Response response = invokeEndpoint(entry.endpoint(), actor);
                if (!isPermissionDenied(response, entry, actor)) {
                    failures.add(formatFailure(entry, actor, response));
                }
            }
        }

        assertThat(failures)
                .as("Disallowed actors were not denied for some endpoints:\n%s", String.join("\n\n", failures))
                .isEmpty();
    }

    @Test
    @DisplayName("permissions.md entries should be valid and unique")
    void shouldValidatePermissionsFileFormatAndRoles() throws IOException {
        List<String> lines = Files.readAllLines(PERMISSIONS_FILE_PATH);
        Set<PermissionEntry> entries = new LinkedHashSet<>();
        List<String> malformedLines = new ArrayList<>();
        List<String> unknownRoles = new ArrayList<>();
        List<String> duplicateEntries = new ArrayList<>();

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.contains("contains") || line.startsWith("#")) {
                continue;
            }

            Matcher matcher = PERMISSION_LINE_PATTERN.matcher(line);
            if (!matcher.matches()) {
                malformedLines.add(line);
                continue;
            }

            String normalizedRole = normalizeRole(matcher.group(1));
            if (!VALID_ROLES.contains(normalizedRole)) {
                unknownRoles.add(line);
            }

            PermissionEntry entry = new PermissionEntry(
                    normalizedRole,
                    new EndpointSignature(
                            matcher.group(2),
                            matcher.group(3),
                            matcher.group(4)
                    )
            );

            if (!entries.add(entry)) {
                duplicateEntries.add(line);
            }
        }

        assertThat(malformedLines)
                .as("Malformed permission lines in permissions.md:\n%s", String.join("\n", malformedLines))
                .isEmpty();
        assertThat(unknownRoles)
                .as("Unknown roles in permissions.md:\n%s", String.join("\n", unknownRoles))
                .isEmpty();
        assertThat(duplicateEntries)
                .as("Duplicate endpoint entries in permissions.md:\n%s", String.join("\n", duplicateEntries))
                .isEmpty();
    }

    private Set<PermissionEntry> parsePermissionsFile() throws IOException {
        List<String> lines = Files.readAllLines(PERMISSIONS_FILE_PATH);
        Set<PermissionEntry> entries = new LinkedHashSet<>();

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.contains("contains") || line.startsWith("#")) {
                continue;
            }

            Matcher matcher = PERMISSION_LINE_PATTERN.matcher(line);
            if (!matcher.matches()) {
                continue;
            }

            entries.add(new PermissionEntry(
                    normalizeRole(matcher.group(1)),
                    new EndpointSignature(
                            matcher.group(2),
                            matcher.group(3),
                            matcher.group(4)
                    )
            ));
        }

        return entries;
    }

    private Set<EndpointSignature> discoverApiEndpoints() {
        Set<EndpointSignature> endpoints = new LinkedHashSet<>();

        for (var entry : handlerMapping.getHandlerMethods().entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();

            Set<String> paths = mappingInfo.getPatternValues();
            if (paths.isEmpty()) {
                continue;
            }

            Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();
            Set<RequestMethod> effectiveMethods = methods.isEmpty()
                    ? EnumSet.of(
                    RequestMethod.GET,
                    RequestMethod.POST,
                    RequestMethod.PUT,
                    RequestMethod.PATCH,
                    RequestMethod.DELETE
            )
                    : methods;

            for (String path : paths) {
                if (!path.startsWith("/api")) {
                    continue;
                }

                for (RequestMethod method : effectiveMethods) {
                    endpoints.add(new EndpointSignature(
                            method.name(),
                            path,
                            handlerMethod.getBeanType().getSimpleName() + "#" + handlerMethod.getMethod().getName()
                    ));
                }
            }
        }

        return endpoints;
    }

    private String joinSignatures(Set<EndpointSignature> signatures) {
        return signatures.stream()
                .sorted(Comparator
                        .comparing(EndpointSignature::path)
                        .thenComparing(EndpointSignature::method)
                        .thenComparing(EndpointSignature::handler))
                .map(s -> s.method() + " " + s.path() + " -> " + s.handler())
                .collect(Collectors.joining("\n"));
    }

    private String normalizeRole(String rawRole) {
        if ("TACHER".equals(rawRole)) {
            return "TEACHER";
        }
        return rawRole;
    }

    private boolean shouldBeDenied(PermissionEntry entry, Actor actor) {
        String requiredRole = entry.role();
        if ("NONE".equals(requiredRole)) {
            return false;
        }
        if ("WORKER".equals(requiredRole)) {
            return true;
        }
        if (actor == Actor.ANONYMOUS) {
            return !isAnonymousAccessible(entry.endpoint().path());
        }
        if (!hasRequiredRole(actor, requiredRole)) {
            return true;
        }
        return actor == Actor.NON_MEMBER_STUDENT
                && "STUDENT".equals(requiredRole)
                && isNonMemberDeniedPath(entry.endpoint().path());
    }

    private boolean hasRequiredRole(Actor actor, String requiredRole) {
        return switch (requiredRole) {
            case "STUDENT" -> actor == Actor.NON_MEMBER_STUDENT
                    || actor == Actor.MEMBER_STUDENT
                    || actor == Actor.TEACHER
                    || actor == Actor.ADMIN;
            case "TEACHER" -> actor == Actor.TEACHER || actor == Actor.ADMIN;
            case "ADMIN" -> actor == Actor.ADMIN;
            default -> false;
        };
    }

    private boolean isNonMemberDeniedPath(String path) {
        // Keep this list strict and explicit; non-member checks are business-rule specific.
        return "/api/submit".equals(path);
    }

    private boolean isAnonymousAccessible(String path) {
        if (path.equals("/api/login")
                || path.equals("/api/logout")
                || path.equals("/api/register")
                || path.equals("/api/contests")
                || path.equals("/api/csrf")
                || path.equals("/api/custom-test")
                || path.equals("/api/custom-test/{key}")
                || path.equals("/api/room/1/posts")) {
            return true;
        }

        return path.startsWith("/api/contest/")
                && (path.endsWith("/registrants")
                || path.endsWith("/is-registered")
                || path.endsWith("/standings")
                || path.endsWith("/submissions")
                || path.endsWith("/status")
                || path.endsWith("/tasks")
                || path.endsWith("/task-names")
                || path.matches("/api/contest/\\{(id|contestId)}"));
    }

    private boolean isPermissionDenied(Response response, PermissionEntry entry, Actor actor) {
        int expectedStatus = expectedDeniedStatus(entry, actor);
        return response.getStatusCode() == expectedStatus;
    }

    private String formatFailure(PermissionEntry entry, Actor actor, Response response) {
        int expectedStatus = expectedDeniedStatus(entry, actor);
        return String.format(
                "%s %s -> %s expected status=%d for %s but got status=%d body=%s",
                entry.endpoint().method(),
                entry.endpoint().path(),
                entry.endpoint().handler(),
                expectedStatus,
                actor.name(),
                response.getStatusCode(),
                truncate(safeBody(response), 400)
        );
    }

    private int expectedDeniedStatus(PermissionEntry entry, Actor actor) {
        if ("WORKER".equals(entry.role())
                || "AdminController#heartbeat".equals(entry.endpoint().handler())) {
            // Worker heartbeat uses a dedicated HTTP Basic + stateless chain.
            // Session-authenticated non-worker users are still unauthorized there.
            return 401;
        }
        return actor == Actor.ANONYMOUS ? 401 : 403;
    }

    private String safeBody(Response response) {
        try {
            String body = response.getBody().asString();
            return body == null ? "" : body;
        } catch (Exception e) {
            return "";
        }
    }

    private String truncate(String input, int maxLen) {
        if (input == null) {
            return "";
        }
        return input.length() <= maxLen ? input : input.substring(0, maxLen) + "...";
    }

    private Response invokeEndpoint(EndpointSignature endpoint, Actor actor) {
        String resolvedPath = resolvePath(endpoint.path());
        String relativePath = resolvedPath.substring("/api".length());
        RequestSpecification request = requestFor(actor);
        Map<String, Object> queryParams = defaultQueryParams(endpoint, resolvedPath);
        if (!queryParams.isEmpty()) {
            request.queryParams(queryParams);
        }

        return switch (endpoint.method()) {
            case "GET" -> request.when().get(relativePath);
            case "POST" -> invokePost(request, relativePath, resolvedPath);
            case "PUT" -> request.body(defaultJsonBody(resolvedPath, "PUT")).when().put(relativePath);
            case "PATCH" -> request.body(defaultJsonBody(resolvedPath, "PATCH")).when().patch(relativePath);
            case "DELETE" -> invokeDelete(request, relativePath, resolvedPath);
            default -> throw new IllegalArgumentException("Unsupported method: " + endpoint.method());
        };
    }

    private Response invokePost(RequestSpecification request, String relativePath, String resolvedPath) {
        if (resolvedPath.endsWith("/post/" + postId + "/image") || resolvedPath.endsWith("/task/" + task.getId() + "/image")) {
            return request.contentType("multipart/form-data")
                    .multiPart("file", "image.png", "fake-image".getBytes())
                    .when()
                    .post(relativePath);
        }
        if (resolvedPath.endsWith("/task/" + task.getId() + "/testcase")) {
            return request.contentType("multipart/form-data")
                    .multiPart("inputFile", "in.txt", "1".getBytes())
                    .multiPart("outputFile", "out.txt", "1".getBytes())
                    .when()
                    .post(relativePath);
        }
        if (resolvedPath.endsWith("/task/" + task.getId() + "/testcases")) {
            return request.contentType("multipart/form-data")
                    .multiPart("file", "tests.zip", "fake-zip".getBytes())
                    .when()
                    .post(relativePath);
        }
        return request.body(defaultJsonBody(resolvedPath, "POST")).when().post(relativePath);
    }

    private Response invokeDelete(RequestSpecification request, String relativePath, String resolvedPath) {
        if (resolvedPath.endsWith("/task/" + task.getId() + "/testcases")) {
            return request.body(defaultJsonBody(resolvedPath, "DELETE")).when().delete(relativePath);
        }
        return request.when().delete(relativePath);
    }

    private RequestSpecification requestFor(Actor actor) {
        return switch (actor) {
            case ANONYMOUS -> givenAnonymous();
            case NON_MEMBER_STUDENT -> givenUser(nonMemberStudent.getUsername(), PASSWORD);
            case MEMBER_STUDENT -> givenUser(memberStudent.getUsername(), PASSWORD);
            case TEACHER -> givenUser(teacher.getUsername(), PASSWORD);
            case ADMIN -> givenUser(admin.getUsername(), PASSWORD);
        };
    }

    private String resolvePath(String originalPath) {
        String path = originalPath;
        path = path.replace("{contestId}", String.valueOf(contest.getId()));
        path = path.replace("{roomId}", String.valueOf(room.getId()));
        path = path.replace("{taskId}", String.valueOf(task.getId()));
        path = path.replace("{postId}", String.valueOf(postId));
        path = path.replace("{commentId}", String.valueOf(commentId));
        path = path.replace("{workerId}", "worker-1");
        path = path.replace("{username}", memberStudent.getUsername());
        path = path.replace("{userId}", String.valueOf(memberStudent.getId()));
        path = path.replace("{status}", "SOLVED");
        path = path.replace("{filename}", "file.png");
        path = path.replace("{language}", "KA");
        path = path.replace("{testKey}", "1");
        path = path.replace("{key}", "custom-test-key");
        path = path.replace("{link}", "invalid-link");

        if (path.contains("/contest/{id}")) {
            path = path.replace("{id}", String.valueOf(contest.getId()));
        } else if (path.contains("/room/{id}/tasks")) {
            path = path.replace("{id}", String.valueOf(room.getId()));
        } else if (path.contains("/task/{id}")) {
            path = path.replace("{id}", String.valueOf(task.getId()));
        } else if (path.contains("/submission/{id}")) {
            path = path.replace("{id}", String.valueOf(submissionId));
        }
        return path;
    }

    private Map<String, Object> defaultQueryParams(EndpointSignature endpoint, String resolvedPath) {
        Map<String, Object> query = new HashMap<>();
        if (resolvedPath.contains("/posts/") && resolvedPath.contains("/comments")) {
            query.put("pageNum", 0);
            query.put("pageSize", 10);
        }
        if ("POST".equals(endpoint.method()) && resolvedPath.endsWith("/task/" + task.getId() + "/testcases")) {
            query.put("taskId", task.getId());
        }
        return query;
    }

    private Object defaultJsonBody(String resolvedPath, String method) {
        if (resolvedPath.equals("/api/login")) {
            return Map.of("username", memberStudent.getUsername(), "password", PASSWORD, "rememberMe", false);
        }
        if (resolvedPath.equals("/api/register")) {
            return Map.of(
                    "username", "permRegisteredUser",
                    "password", "password123",
                    "email", "permRegisteredUser@test.com",
                    "firstName", "Perm",
                    "lastName", "User"
            );
        }
        if (resolvedPath.equals("/api/recover/request")) {
            return Map.of("username", memberStudent.getUsername());
        }
        if (resolvedPath.contains("/recover/update-password/")) {
            return Map.of("newPassword", "newPass123");
        }
        if (resolvedPath.equals("/api/contest")) {
            return Map.of(
                    "name", "Created Contest",
                    "roomId", room.getId(),
                    "durationInSeconds", 3600,
                    "upsolvingAfterFinish", false,
                    "upsolving", false,
                    "scoringType", "BEST_SUBMISSION"
            );
        }
        if (resolvedPath.matches("/api/contest/\\d+$") && "PUT".equals(method)) {
            return Map.of(
                    "name", "Updated Contest",
                    "roomId", room.getId(),
                    "durationInSeconds", 3600,
                    "upsolvingAfterFinish", false,
                    "upsolving", false,
                    "scoringType", "BEST_SUBMISSION"
            );
        }
        if (resolvedPath.endsWith("/tasks/order")) {
            return Map.of("taskIds", List.of(task.getId()));
        }
        if (resolvedPath.equals("/api/submit")) {
            return Map.of(
                    "contestId", contest.getId().intValue(),
                    "taskId", task.getId().intValue(),
                    "language", "CPP",
                    "submissionText", "#include <iostream>\nint main(){return 0;}"
            );
        }
        if (resolvedPath.equals("/api/task")) {
            return Map.ofEntries(
                    Map.entry("taskId", task.getId()),
                    Map.entry("contestId", contest.getId().intValue()),
                    Map.entry("code", "B"),
                    Map.entry("title", "New Task"),
                    Map.entry("taskType", "BATCH"),
                    Map.entry("taskScoreType", "SUM"),
                    Map.entry("taskScoreParameter", "100"),
                    Map.entry("timeLimitMillis", 1000),
                    Map.entry("memoryLimitMB", 256),
                    Map.entry("checkerType", "TOKEN"),
                    Map.entry("inputTemplate", "test*.in"),
                    Map.entry("outputTemplate", "test*.out")
            );
        }
        if (resolvedPath.contains("/statement")) {
            return Map.of("statement", "Sample statement", "language", "KA");
        }
        if (resolvedPath.endsWith("/testcases/1/public")) {
            return Map.of("status", true);
        }
        if (resolvedPath.endsWith("/testcases") && "DELETE".equals(method)) {
            return Map.of("testKeys", List.of("1"));
        }
        if (resolvedPath.equals("/api/admin/workers")) {
            return Map.of("count", 1);
        }
        if (resolvedPath.contains("/heartbeat")) {
            return Map.of("jobsProcessed", 1, "working", false);
        }
        if (resolvedPath.endsWith("/comment")) {
            Map<String, Object> body = new HashMap<>();
            body.put("content", "comment");
            body.put("parentId", null);
            return body;
        }
        if (resolvedPath.matches("/api/post/\\d+$") && "PUT".equals(method)) {
            return Map.of(
                    "id", postId,
                    "title", "Post title",
                    "content", "Post content",
                    "draftContent", "{\"title\":\"Post title\",\"body\":\"Post content\"}",
                    "roomId", room.getId(),
                    "status", "PUBLISHED",
                    "version", 1
            );
        }
        if (resolvedPath.matches("/api/room/\\d+/post$")) {
            Map<String, Object> body = new HashMap<>();
            body.put("id", postId);
            body.put("title", "Draft title");
            body.put("content", null);
            body.put("draftContent", "{\"title\":\"Draft title\",\"body\":\"Draft content\"}");
            body.put("roomId", room.getId());
            body.put("status", "DRAFT");
            body.put("version", 1);
            return body;
        }
        if (resolvedPath.equals("/api/custom-test")) {
            return Map.of(
                    "code", "#include <iostream>\nint main(){return 0;}",
                    "language", "CPP",
                    "input", "1"
            );
        }
        if (resolvedPath.equals("/api/user/change-password")) {
            return Map.of("oldPassword", PASSWORD, "newPassword", "updatedPassword123");
        }
        return Map.of();
    }

    private Long createDraftPostAsTeacher() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", "Permission Draft");
        payload.put("content", null);
        payload.put("draftContent", "{\"title\":\"Permission Draft\",\"body\":\"Draft content\"}");
        payload.put("roomId", room.getId());
        payload.put("status", "DRAFT");
        payload.put("version", 1);

        Response response = givenUser(teacher.getUsername(), PASSWORD)
                .body(payload)
                .when()
                .post("/room/{roomId}/post", room.getId());

        assertThat(response.statusCode()).isEqualTo(200);
        return response.jsonPath().getLong("post.id");
    }

    private Long createCommentAsMember(Long createdPostId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("content", "Initial comment");
        payload.put("parentId", null);

        Response response = givenUser(memberStudent.getUsername(), PASSWORD)
                .body(payload)
                .when()
                .post("/posts/{postId}/comment", createdPostId);

        assertThat(response.statusCode()).isEqualTo(200);

        return commentJpaRepository.findAll().stream()
                .filter(comment -> Objects.equals(comment.getPostId(), createdPostId))
                .map(PostComment::getId)
                .max(Long::compareTo)
                .orElse(1L);
    }

    private enum Actor {
        ANONYMOUS,
        NON_MEMBER_STUDENT,
        MEMBER_STUDENT,
        TEACHER,
        ADMIN
    }

    private record PermissionEntry(
            String role,
            EndpointSignature endpoint
    ) {
    }

    private record EndpointSignature(
            String method,
            String path,
            String handler
    ) {
    }
}
