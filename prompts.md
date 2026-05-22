This document outlines the sequential prompts and requirements used to build the `IssueFlow` Spring Boot REST API, along with the specific AI model each prompt was targeted at.

## Phase 1: Core Entities (Google Gemini Pro 3.1)

Generate the Spring Data JPA Entity classes for the IssueFlow project under the package `com.att.tdp.issueflow.entities`.

Please create the following classes: `User`, `Project`, `Ticket`, and `Comment`.

Requirements for each entity:

1. Include standard JPA annotations (`@Entity`, `@Table`, `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`).
2. Use Lombok annotations (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`) to reduce boilerplate.
3. Include creation and update timestamps (e.g., `@CreationTimestamp`, `@UpdateTimestamp`).
4. Apply basic Jakarta validation annotations on fields (e.g., `@NotBlank`, `@NotNull`).
5. Map the relationships correctly based on standard issue-tracking logic:
* A Project has many Tickets.
* A Ticket belongs to one Project and has one User as an assignee.
* A Ticket has many Comments.
* A Comment belongs to one Ticket and has one User as the author.



## Phase 2: User Entity Enhancements (Google Gemini Pro 3.1)

Update the `User` entity in `com.att.tdp.issueflow.entities` to include the following new fields:

1. `fullName`: A string field for the user's full name.
2. `role`: An Enum field restricted to `DEVELOPER` and `ADMIN`.

Create the `Role` enum class and ensure the `role` field in the `User` entity is mapped as a string in the database using `@Enumerated(EnumType.STRING)`.

## Phase 3: Repository Layer (Claude Code Sonnet 4.6)

Generate the Spring Data JPA Repository interfaces for the IssueFlow project under the package `com.att.tdp.issueflow.repository`.

Please create the following interfaces extending `JpaRepository`:

1. `UserRepository`: Add methods `Optional<User> findByUsername(String username)`, `boolean existsByUsername(String username)`, and `boolean existsByEmail(String email)`.
2. `ProjectRepository`: Standard JpaRepository.
3. `TicketRepository`: Add methods to find tickets by Project ID and by Assignee ID.
4. `CommentRepository`: Add a method to find all comments by Ticket ID.

## Phase 4: Data Transfer Objects (DTOs) (Claude Code Sonnet 4.6)

Please implement the Data Transfer Objects (DTOs) for the Project, Ticket, and Comment subdomains following a highly organized, modular package-by-feature pattern matching the layout used for Users.

Create all DTOs as standard classes using Lombok `@Data`, `@Builder`, `@NoArgsConstructor`, and `@AllArgsConstructor` annotations. Apply Jakarta field validation annotations (`@NotBlank`, `@NotNull`, `@Size`, etc.) to incoming payloads.

Please structure and write the following files:

**1. Under package `com.att.tdp.issueflow.dto.ProjectDTO`:**

* `AddProjectDTO.java`: Fields for `name` (NotBlank) and `description`.
* `UpdateProjectDTO.java`: Fields for `name` (NotBlank) and `description`.
* `ProjectResponseDTO.java`: Fields for `id`, `name`, `description`, `createdAt`, and `updatedAt`.

**2. Under package `com.att.tdp.issueflow.dto.TicketDTO`:**

* `AddTicketDTO.java`: Fields for `title` (NotBlank), `description`, and `projectId` (NotNull). (Do not include assigneeId here as tickets start unassigned or handled separately).
* `UpdateTicketDTO.java`: Fields for `title` (NotBlank), `description`, and `assigneeId` (to allow setting or changing who is working on it).
* `TicketResponseDTO.java`: Fields for `id`, `title`, `description`, `projectId`, `assigneeId` (nullable), `createdAt`, and `updatedAt`.

**3. Under package `com.att.tdp.issueflow.dto.CommentDTO`:**

* `AddCommentDTO.java`: Fields for `content` (NotBlank) and `ticketId` (NotNull). (The authorId should be extracted from the authenticated user context later, so do not include it in the request payload).
* `CommentResponseDTO.java`: Fields for `id`, `content`, `ticketId`, `authorId`, `createdAt`, and `updatedAt`.

*Note: Ensure all cross-references in Response DTOs flatten out deep entity graphs into primitive IDs (e.g., using `Long projectId` or `Long assigneeId` instead of nesting the whole entity object) to keep API data representations decoupled, clean, and lightweight.*

## Phase 5: Exception Handling Infrastructure - Initial (Claude Code Sonnet 4.6)

Please create a standardized exception-handling infrastructure for the IssueFlow project under the package `com.att.tdp.issueflow.exception`.

I need two files created with the following exact behaviors:

**1. Create a utility class named `Utils.java` to format standardized JSON error responses:**

* Make the class final with a private constructor that throws an AssertionError to enforce the utility pattern.
* Implement a static method: `public static ResponseEntity<Object> buildErrorResponse(Exception e, HttpStatus status, WebRequest request)`
* The method should return a ResponseEntity containing a HashMap with these exact keys:
* "timestamp" -> LocalDateTime.now()
* "status" -> status.value()
* "error" -> status.getReasonPhrase()
* "message" -> e.getMessage()
* "path" -> request.getDescription(false)



**2. Create a global handler class named `GlobalExceptionHandler.java`:**

* Annotate it with `@RestControllerAdvice`.
* Use a static import for `com.att.tdp.issueflow.exception.Utils.buildErrorResponse`.
* Implement `@ExceptionHandler` methods for the following exception types:
* `ResourceNotFoundException.class` -> Returns NOT_FOUND via buildErrorResponse.
* `BadRequestException.class` -> Returns BAD_REQUEST via buildErrorResponse.
* `SQLException.class` -> Returns INTERNAL_SERVER_ERROR via buildErrorResponse.
* `RuntimeException.class` -> Returns INTERNAL_SERVER_ERROR via buildErrorResponse.
* `MethodArgumentTypeMismatchException.class` -> Returns BAD_REQUEST via buildErrorResponse with a custom message.
* `HttpMessageNotReadableException.class` -> Returns BAD_REQUEST via buildErrorResponse with a custom message.
* `MethodArgumentNotValidException.class` -> Manually construct a ResponseEntity for field validation errors.
* `ConstraintViolationException.class` -> Manually construct a similar validation ResponseEntity.



Ensure all components use standard Spring Boot 3 / Jakarta web annotations and match a clean production-grade REST architecture.

## Phase 6: Exception Handling Infrastructure - Refined (Claude Code Sonnet 4.6)

Please build a specialized, production-grade exception handling system for the IssueFlow project under the package `com.att.tdp.issueflow.exception`. It should follow a robust REST architecture, creating two central files:

**1. Create a utility class named `Utils.java`:**

* It must be a final class with a private constructor throwing an AssertionError.
* Implement a static helper method: `public static ResponseEntity<Object> buildErrorResponse(Exception e, HttpStatus status, WebRequest request)`
* It should map errors into a standardized JSON response body containing: `timestamp`, `status`, `error`, `message`, `path`.

**2. Create a global error coordinator named `GlobalExceptionHandler.java`:**

* Annotate it with `@RestControllerAdvice`.
* Use a static import for `com.att.tdp.issueflow.exception.Utils.buildErrorResponse`.
* Implement handlers mapping the following exception cases to precise API contracts:
* `ResourceNotFoundException.class` -> Returns HttpStatus.NOT_FOUND (404) via buildErrorResponse.
* `BadRequestException.class` -> Returns HttpStatus.BAD_REQUEST (400) via buildErrorResponse.
* `HttpMessageNotReadableException.class` -> Returns HttpStatus.BAD_REQUEST (400) with a clean message.
* `MethodArgumentTypeMismatchException.class` -> Returns HttpStatus.BAD_REQUEST (400) parsing path/query parameters dynamically.
* `MethodArgumentNotValidException.class` -> For handling @Valid input constraints. Return a manual ResponseEntity containing an "errors" collection.
* `ConstraintViolationException.class` -> For handling parameter constraints. Return a similar structured validation payload.
* `Exception.class` (Generic fallback) -> Returns HttpStatus.INTERNAL_SERVER_ERROR (500) via buildErrorResponse.



Ensure all classes compile perfectly with Java 21 and Spring Boot 3 dependencies.

## Phase 7: Service Layer Implementation (Claude Code Sonnet 4.6)

Please implement the Service layers for Projects, Tickets, and Comments under the package `com.att.tdp.issueflow.service`. These services must use the core entities, repositories, exceptions, and DTOs we have already set up.

Ensure you implement the following files and include these specific business constraints from the requirements:

**1. `ProjectService.java`:**

* `createProject(AddProjectDTO dto)`
* `getProjectById(Long id)`
* `getAllProjects()`
* `updateProject(Long id, UpdateProjectDTO dto)`
* `deleteProject(Long id)` (Implement soft deletion logic/checks)

**2. `TicketService.java`:**

* `createTicket(AddTicketDTO dto)`
* `getTicketById(Long id)`
* `getTicketsByProject(Long projectId)`
* `updateTicket(Long id, UpdateTicketDTO dto)`
* Advanced Feature Prep (TODO Hooks): `autoAssignUnassignedTickets()` and `checkDependenciesBeforeClose()`

**3. `CommentService.java`:**

* `addComment(AddCommentDTO dto, Long authenticatedUserId)`
* `getCommentsByTicket(Long ticketId)`
* Mentions Feature Prep (TODO Hook): Parse comment text for @username tokens.

Ensure all mapping logic between Entities and DTOs handles null safety gracefully.

## Phase 8: Domain Enhancements - Ticket Status (Claude Code Sonnet 4.6)

Update the `Ticket` domain to support ticket statuses as required by the system specification:

1. Create a `Status` enum under `com.att.tdp.issueflow.entities` containing: `OPEN`, `IN_PROGRESS`, `CLOSED`.
2. Update `Ticket.java` to include a `private Status status;` field. Ensure it is annotated with `@Enumerated(EnumType.STRING)` and `@NotNull`.
3. Verify that `TicketService.java` correctly sets the initial status to `Status.OPEN` inside the ticket creation method.

## Phase 9: Domain Enhancements - Requirements Compliance (Claude Code Sonnet 4.6)

Please review the `TDP_issueflow_requirements.pdf` and update our JPA Entities to strictly comply with the document. Specifically, focus on the `Ticket` entity:

1. Implement the precise `Status` enum values required: `TODO`, `IN_PROGRESS`, `IN_REVIEW`, and `DONE`.
2. Implement the `Priority` enum (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`) and the `Type` enum (`BUG`, `FEATURE`, `TECHNICAL`).
3. Add the missing `dueDate` (timestamp) and `is_overdue` (boolean) fields to support the auto-escalation feature.
4. Implement Optimistic Locking by adding a `@Version private Long version;` field to the Ticket and Comment entities to satisfy the concurrency control requirements (preventing simultaneous edits).

## Phase 10: DTO Refactoring - Requirements Compliance (Claude Code Sonnet 4.6)

Please read the API table in the `README.md` and the functional requirements in `TDP_issueflow_requirements.pdf` to refactor our Data Transfer Objects (DTOs). You must ensure the payloads match exactly:

1. `AddProjectDTO`: Must include an `ownerId` field.
2. `AddTicketDTO`: Must accept `status`, `priority`, `type`, and an optional `assigneeId` right at creation.
3. `AddCommentDTO`: Must explicitly require the `authorId` in the JSON body.

Audit all other DTOs and ensure no required properties from the documentation are missing.

## Phase 11: Implementing the REST Controllers (Claude Code Sonnet 4.6)

Please implement the REST Controller layer for Users, Projects, Tickets, and Comments under `com.att.tdp.issueflow.controller`.

CRITICAL: You must strictly adhere to the exact HTTP verbs and paths defined in the grading contract. Use `@RestController`. Inject services via `@RequiredArgsConstructor`. Apply `@Valid` on all request bodies.

Please create the following controllers:

1. `UserController.java`
* `POST /users/update/{userId}`: Update user details (accepts `UpdateUserDTO`). Note the non-standard POST verb.
* (Skip login/register for now, we will do Auth next).


2. `ProjectController.java` (Base: `/projects`)
* `POST /`: Create project (accepts `AddProjectDTO`).
* `GET /`: Fetch all projects.
* `GET /{projectId}`: Fetch single project.
* `PATCH /{projectId}`: Update project (accepts `UpdateProjectDTO`). Note the PATCH verb.
* `DELETE /{projectId}`: Soft delete project.


3. `TicketController.java` (Base: `/tickets`)
* `POST /`: Create ticket (accepts `AddTicketDTO`).
* `GET /{ticketId}`: Fetch single ticket.
* `GET /`: Fetch tickets by project. Must use a query parameter (e.g., `?projectId=1`).
* `PATCH /{ticketId}`: Update ticket (accepts `UpdateTicketDTO`). Note the PATCH verb.
* `DELETE /{ticketId}`: Soft delete ticket.


4. `CommentController.java`
* Note: Do NOT use a generic /comments base path. These must be nested under tickets.
* `POST /tickets/{ticketId}/comments`: Add comment (accepts `AddCommentDTO`).
* `GET /tickets/{ticketId}/comments`: Fetch all comments for a ticket.



Ensure all paths, path variables (`{projectId}`, `{ticketId}`), and HTTP verbs match this layout exactly.

## Phase 12: Adding Security Dependencies (Claude Code Sonnet 4.6)

Update the pom.xml file to add the dependencies required for Spring Security and JWT authentication to fulfill the project requirement: "The system must protect all API endpoints using JWT-based authentication."
Please add the following dependencies:

spring-boot-starter-security
java-jwt (groupId: com.auth0, artifactId: java-jwt, latest 4.x version) OR the jjwt library suite (api, impl, and jackson).
Ensure that you ONLY add these new dependencies. Do not remove, downgrade, or alter any of the existing dependencies (such as commons-csv, spring-boot-starter-data-jpa, postgresql, or lombok). Keep the formatting consistent.

## Phase 13: The JWT Utility (Claude Code Sonnet 4.6)

Please create a JWT utility class for our authentication system under a new package: `com.att.tdp.issueflow.security`.

Create a file named `JwtUtil.java` with the following requirements:

1. Annotate the class with `@Component` so Spring can manage it.
2. Use `@Value` to inject a secret key and expiration time. Provide safe fallback defaults in the annotation, for example: `@Value("${jwt.secret:MySuperSecretKeyForIssueFlowDevelopment1234567890}")` and `@Value("${jwt.expiration:86400000}")` (24 hours).
3. Implement a method `public String generateToken(User user)` that builds a JWT. The token subject should be the `username`, and you should add a custom claim for the user's `role`.
4. Implement a method `public String extractUsername(String token)` to read the subject from the token.
5. Implement a method `public boolean isTokenValid(String token, UserDetails userDetails)` to check expiration and matching usernames.

Use the exact JWT library that you just added to the `pom.xml` (either `com.auth0.jwt` or `io.jsonwebtoken`). Ensure the code handles signing algorithms securely according to modern standards for that specific library.

## Phase 14: Security Filter and User Details Service (Claude Code Sonnet 4.6)

Please implement the components necessary to intercept HTTP requests and load user data for our Spring Security setup under the package `com.att.tdp.issueflow.security`.

Please create the following two files:

1. `CustomUserDetailsService.java`:
* Annotate with `@Service`.
* Implement Spring Security's `UserDetailsService` interface.
* Inject the `UserRepository` using constructor injection.
* Implement the `loadUserByUsername(String username)` method. Fetch the user from the database; if not found, throw a `UsernameNotFoundException`.
* Map your custom `User` entity to a standard Spring Security `org.springframework.security.core.userdetails.User` object. Ensure you map the user's `role` to a Spring Security `SimpleGrantedAuthority` (e.g., prefixing the role with "ROLE_").


2. `JwtAuthenticationFilter.java`:
* Annotate with `@Component`.
* Extend `OncePerRequestFilter`.
* Inject `JwtUtil` and `CustomUserDetailsService` using constructor injection.
* Override the `doFilterInternal` method.
* Logic: Extract the "Authorization" header. If it is null or does not start with "Bearer ", call `filterChain.doFilter` and return.
* If a token is found, extract it (remove "Bearer ") and pull the username using `JwtUtil.extractUsername()`.
* If the username is not null and `SecurityContextHolder.getContext().getAuthentication()` is null, load the `UserDetails` using your `CustomUserDetailsService`.
* Validate the token using `JwtUtil.isTokenValid()`. If valid, create a `UsernamePasswordAuthenticationToken`, attach the request details via `new WebAuthenticationDetailsSource().buildDetails(request)`, and set it in the `SecurityContextHolder`.
* Always ensure `filterChain.doFilter(request, response)` is called at the end.



## Phase 15: The Security Configuration (Claude Code Sonnet 4.6)

Please create the main security configuration class for the application under the package `com.att.tdp.issueflow.security`.

Create a file named `SecurityConfig.java` with the following requirements:

1. Annotate the class with `@Configuration` and `@EnableWebSecurity`.
2. Inject your custom `JwtAuthenticationFilter` and `CustomUserDetailsService` via constructor injection (Lombok's `@RequiredArgsConstructor`).
3. Create a `@Bean` for `PasswordEncoder` that returns a new `BCryptPasswordEncoder`.
4. Create a `@Bean` for `AuthenticationProvider` that returns a `DaoAuthenticationProvider`. Set its UserDetailsService to your custom service and its PasswordEncoder to the BCrypt bean.
5. Create a `@Bean` for `AuthenticationManager` using `AuthenticationConfiguration.getAuthenticationManager()`.
6. Create the main `@Bean` for `SecurityFilterChain(HttpSecurity http)`. You must configure it using the modern Lambda DSL (Spring Boot 3 / Spring Security 6 style):
* Disable CSRF (`http.csrf(csrf -> csrf.disable())`).
* Set session management to stateless (`http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))`).
* Configure authorization rules:
* Permit all `POST` requests to `/users` (so people can register).
* Permit all requests to `/auth/` (so people can log in and out).
* Require authentication for any other request (`anyRequest().authenticated()`).


* Set the authentication provider to your custom provider bean.
* Add your `jwtAuthenticationFilter` BEFORE the standard `UsernamePasswordAuthenticationFilter`.



## Phase 16: Authentication Controller & Service (Claude Code Sonnet 4.6)

Please implement the final pieces of the JWT Authentication system to satisfy the project requirements.

1. Create the DTOs under `com.att.tdp.issueflow.dto.AuthDTO`:
* `LoginRequestDTO.java`: Fields for `username` (@NotBlank) and `password` (@NotBlank).
* `AuthResponseDTO.java`: Field for `token` (String).


2. Create the Service under `com.att.tdp.issueflow.service.AuthService`:
* Inject `AuthenticationManager`, `JwtUtil`, and your `UserRepository` or `UserService`.
* Implement a `login` method that takes `LoginRequestDTO`.
* Inside `login`: Use `authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password))`.
* If successful, fetch the User from the database, use `JwtUtil.generateToken(username, role)` to create the token, and return it in an `AuthResponseDTO`.


3. Create the Controller under `com.att.tdp.issueflow.controller.AuthController`:
* Base path: `/auth`.
* `POST /login`: Accepts `@Valid LoginRequestDTO`, calls `AuthService.login`, and returns the `AuthResponseDTO`.
* `POST /logout`: Since JWT is stateless, simply return an HTTP 200 OK with a generic message like "Logged out successfully" (the client will handle deleting the token).
* `GET /me`: Get the current username via `SecurityContextHolder.getContext().getAuthentication().getName()`, fetch that user's details from the database, and return them (ensure you do not return the password field).



## Phase 17: Auto-Assignment Feature (Claude Code Sonnet 4.6)

Please implement the Auto-Assignment feature as defined in Section 3.8 of the requirements.

1. Create a DTO `com.att.tdp.issueflow.dto.ProjectDTO.WorkloadDTO`:
* Must contain: `Long userId`, `String username`, `Long openTicketCount`.


2. Update `UserRepository` (or create a custom query in `TicketRepository`):
* Write a custom JPQL query to calculate developer workloads for a specific project.
* The query MUST fetch ALL users where `role = 'DEVELOPER'`.
* It should `LEFT JOIN` the `Ticket` entity (filtered by the given `projectId` and `status != 'DONE'`) so developers with 0 tickets are included in the results.
* It must return a `List<WorkloadDTO>`.
* Order the results by `openTicketCount` ASC, and then by the user's `id` ASC (to handle ties by registration order).


3. Update `ProjectController` and `ProjectService`:
* Add the endpoint `GET /projects/{projectId}/workload`.
* This endpoint should return the `List<WorkloadDTO>` using the query you just wrote.


4. Update `TicketService.createTicket(AddTicketDTO dto)`:
* Before saving the new ticket, check if `dto.getAssigneeId()` is null.
* If it is null, fetch the workload list for the project.
* If the list is not empty, grab the first user (lowest workload / oldest registrant) and set them as the ticket's assignee.
* If the list is empty (no developers exist), leave the assignee as null.
* Add a comment `// TODO: Audit Log - record AUTO_ASSIGN action` right after setting the assignee (we will implement the audit log later).



## Phase 18: Auto-Escalation Feature (Claude Code Sonnet 4.6)

Please implement the Auto-Escalation feature for Tickets as defined in Section 3.7 of the requirements.

1. Enable Scheduling:
* Add `@EnableScheduling` to the main `IssueFlowApplication.java` class so Spring knows to look for background tasks.


2. Update `TicketRepository`:
* Add a method to find all overdue, active tickets. For example:
`List<Ticket> findByDueDateBeforeAndStatusNot(LocalDateTime time, Status status);`
(We need tickets where the due date is in the past, and the status is NOT 'DONE').


3. Create the Scheduler under `com.att.tdp.issueflow.service.TicketEscalationScheduler`:
* Annotate the class with `@Service` and inject the `TicketRepository`.
* Create a method called `escalateOverdueTickets()`.
* Annotate it with `@Scheduled(cron = "0 * * * * *")`. (This cron expression runs it at the top of every single minute, which is perfect for testing. We can slow it down later).
* Inside the method:
* Fetch all tickets using the repository method you just made, passing `LocalDateTime.now()` and `Status.DONE`.
* For each ticket, apply the escalation logic:
* If Priority is LOW -> Change to MEDIUM.
* If Priority is MEDIUM -> Change to HIGH.
* If Priority is HIGH -> Change to CRITICAL.
* If Priority is CRITICAL -> Set the `isOverdue` boolean flag to `true`.


* Save all updated tickets back to the repository.




4. Update `TicketService.updateTicket(Long id, UpdateTicketDTO dto)`:
* To satisfy the manual override requirement: If the `dto` contains a `priority` that is different from the ticket's current priority, you must clear the escalation flag by setting `ticket.setIsOverdue(false)`.



## Phase 19: Ticket Dependencies Feature (Claude Code Sonnet 4.6)

Please implement the Ticket Dependencies feature as defined in Section 3.2 of the requirements.

1. Update the `Ticket` Entity:
* Add a Many-To-Many relationship to represent dependencies (tickets that MUST be completed before this one).
* Add this field:
@ManyToMany
@JoinTable(
name = "ticket_dependencies",
joinColumns = @JoinColumn(name = "ticket_id"),
inverseJoinColumns = @JoinColumn(name = "depends_on_ticket_id")
)
private Set dependsOn = new HashSet<>();


2. Update `TicketService`:
* Add a method `public void addDependency(Long ticketId, Long dependsOnId)`:
* Fetch both tickets.
* Ensure a ticket cannot depend on itself (throw a `BadRequestException` if IDs match).
* Add the `dependsOn` ticket to the main ticket's `dependsOn` set and save.


* Add a method `public void removeDependency(Long ticketId, Long dependsOnId)`:
* Fetch the ticket, remove the dependency from the set, and save.


* **Crucial Update to `updateTicket`:** - Before saving an updated ticket, check if the incoming DTO is requesting a status change to `DONE`.
* If it is `DONE`, loop through the `dependsOn` set. If ANY ticket in that set has a status that is NOT `DONE`, throw an `IllegalStateException` (or a custom 400 Bad Request exception) with a message like "Cannot close ticket: dependent tickets are not yet DONE."




3. Update `TicketController`:
* Add `POST /tickets/{id}/dependencies/{dependsOnId}` to call the add method. Return 200 OK.
* Add `DELETE /tickets/{id}/dependencies/{dependsOnId}` to call the remove method. Return 204 No Content.



## Phase 20: Audit log feature (Claude Code Sonnet 4.6)

Please implement the Audit Log feature to track ticket history and system actions.

1. Create the Entity `com.att.tdp.issueflow.entity.AuditLog`:
* Fields: `Long id` (Primary Key), `Long ticketId`, `String action` (e.g., "STATUS_CHANGE", "AUTO_ASSIGN"), `String performedBy` (Username of the person or "SYSTEM"), `LocalDateTime timestamp`, `String details` (e.g., "Status changed from TODO to IN_PROGRESS").
* Add standard JPA annotations (`@Entity`, `@Id`, `@GeneratedValue`).


2. Create the DTO `com.att.tdp.issueflow.dto.AuditLogDTO`:
* Map all fields from the entity so we can return clean JSON.


3. Create `AuditLogRepository` and `AuditLogService`:
* Repository should have a method: `List<AuditLog> findByTicketIdOrderByTimestampDesc(Long ticketId);`
* Service should have: `public void logAction(Long ticketId, String action, String performedBy, String details)`
* Service should have: `public List<AuditLogDTO> getLogsForTicket(Long ticketId)`


4. Integrate with `TicketService`:
* Inject `AuditLogService`.
* In `createTicket` (where you left the `// TODO` for Auto-Assign): Call `logAction(ticketId, "AUTO_ASSIGN", "SYSTEM", "Ticket automatically assigned to user ID: " + assigneeId)`.
* In `updateTicket`: If the status changes, call `logAction(ticketId, "STATUS_CHANGE", SecurityContextHolder.getContext().getAuthentication().getName(), "Status updated to " + newStatus)`.


5. Update `TicketController`:
* Add endpoint `GET /tickets/{id}/audit` to return `List<AuditLogDTO>` by calling the service.



## Phase 21: unit testing (Claude Code Sonnet 4.6)

Please implement comprehensive unit tests for our core business logic in the TicketService.

1. Dependency Check:
* Verify that `spring-boot-starter-test` is in our `pom.xml`.


2. Create the Test Class:
* Create `TicketServiceTest.java` in the `src/test/java/com/att/tdp/issueflow/service/` directory.
* Use JUnit 5 and Mockito (`@ExtendWith(MockitoExtension.class)`).
* Mock all dependencies: `TicketRepository`, `UserRepository`, `ProjectRepository`, and `AuditLogService`.
* Inject the mocks into `TicketService`.


3. Implement the following test cases based on the provided reference code:
* `shouldAssignToLeastLoadedDeveloper()`: Tests the Auto-Assignment logic.
* `shouldThrowErrorWhenClosingBlockedTicket()`: Tests the Dependency trap.
* `shouldAllowClosingUnblockedTicket()`: Tests successful closure when dependencies are DONE.
* `shouldThrowErrorOnInvalidStateTransition()`: Tests the strict TODO -> IN_PROGRESS -> IN_REVIEW -> DONE state machine.



Please adapt the provided reference code to perfectly match our exact Entity and DTO field names, constructors, and exception types.

Please implement the remaining unit tests for our core services to achieve high test coverage.

1. Environment setup:
* Ensure these files are created in `src/test/java/com/att/tdp/issueflow/service/`.
* Use JUnit 5 (`@Test`) and Mockito (`@ExtendWith(MockitoExtension.class)`).
* Mock all repository dependencies and inject them into the services.


2. Create `UserServiceTest.java`:
* Mock `UserRepository` and `PasswordEncoder`.
* Test `registerUser`: Ensure successful registration hashes the password and saves the user.
* Test `registerUser`: Ensure it throws a `BadRequestException` (or similar) if the username or email already exists.


3. Create `ProjectServiceTest.java`:
* Mock `ProjectRepository` and `UserRepository`.
* Test `createProject`: Ensure successful creation maps the owner correctly.
* Test `createProject`: Ensure it throws a `ResourceNotFoundException` if the `ownerId` does not exist in the database.


4. Create `CommentServiceTest.java`:
* Mock `CommentRepository`, `TicketRepository`, and `UserRepository`.
* Test `addComment`: Ensure a comment is successfully saved and linked to both the Ticket and the User.
* Test `addComment`: Ensure it throws a `ResourceNotFoundException` if the ticket ID does not exist.



Please match the provided reference code styles and use our exact DTO and Entity field names.

**(I provided claude with the unit testing files I made as a foundation to build on)**

## Phase 22: E2E test (Claude Code Sonnet 4.6)

Please create a comprehensive End-to-End API Integration test that verifies the entire system lifecycle and core business logic in a single run.

1. Create the Test Class:
* Create `IssueFlowE2ETest.java` in `src/test/java/com/att/tdp/issueflow/`.
* Annotate with `@SpringBootTest` and `@AutoConfigureMockMvc`.
* Annotate with `@Transactional` so the database automatically rolls back after the test.


2. Inject Dependencies:
* Inject `MockMvc` to fire HTTP requests.
* Inject `ObjectMapper` to convert map objects to JSON strings.
* Inject `@Autowired TicketEscalationScheduler escalationScheduler;` to manually trigger the background cron job.


3. Write the Golden Path Test (`shouldExecuteFullUserJourney`), executing the following sequence exactly:
* Step 1: Registration. Register User A (dev_alpha). Attempt to register User A again and expect `400 Bad Request`. Register User B (dev_beta). Expect `200 OK`.
* Step 2: Login. Login as User A and extract the JWT token from the JSON response.
* Step 3: Project Creation. POST to `/projects` using the token to create a project. Extract the `projectId`.
* Step 4: Auto-Assignment Setup.
* Create Ticket 1 (The Blocker) with a `dueDate` in the past and `priority` LOW. Verify it auto-assigns to User A (ID 1). Extract `ticket1Id`.
* Create Ticket 2 (The Target). Verify it auto-assigns to User B (ID 2) to balance the workload. Extract `ticket2Id`.


* Step 5: Dependency Setup. POST to `/tickets/{ticket2Id}/dependencies/{ticket1Id}` to make Ticket 2 depend on Ticket 1.
* Step 6: Auto-Escalation. Call `escalationScheduler.escalateOverdueTickets()` directly. GET Ticket 1 and verify its priority escalated from LOW to MEDIUM.
* Step 7: Dependency Trap. Move Ticket 2 through the state machine (`IN_PROGRESS` -> `IN_REVIEW`) via PATCH requests. Attempt to PATCH it to `DONE` and expect a `400 Bad Request` because Ticket 1 is not done.
* Step 8: Resolve Blocker. Move Ticket 1 through the state machine (`IN_PROGRESS` -> `IN_REVIEW` -> `DONE`). Expect `200 OK` for all.
* Step 9: Successful Closure. Attempt to PATCH Ticket 2 to `DONE` again. Expect `200 OK`.
* Step 10: Audit Log Verification. GET `/tickets/{ticket1Id}/audit`. Assert the array size is greater than 0.
* Step 11: Comments Verification. POST a comment to `/tickets/{ticket1Id}/comments`. GET the comments for that ticket and assert the array size is greater than 0 and the text matches the posted comment.



Please ensure all PATCH/POST requests include full valid JSON payloads to satisfy our `@Valid` constraints. Use `JsonPath` for response extraction.