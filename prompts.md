This document outlines the sequential prompts and requirements used to build the `IssueFlow` Spring Boot REST API, along with the specific AI model each prompt was targeted at.

## Phase 1: Core Entities (Google Gemini Pro)
Generate the Spring Data JPA Entity classes for the IssueFlow project under the package `com.att.tdp.issueflow.entities`. 

Please create the following classes: `User`, `Project`, `Ticket`, and `Comment`. 

Requirements for each entity:
1. Include standard JPA annotations (`@Entity`, `@Table`, `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`).
2. Use Lombok annotations (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`) to reduce boilerplate.
3. Include creation and update timestamps (e.g., `@CreationTimestamp`, `@UpdateTimestamp`).
4. Apply basic Jakarta validation annotations on fields (e.g., `@NotBlank`, `@NotNull`).
5. Map the relationships correctly based on standard issue-tracking logic:
   - A Project has many Tickets.
   - A Ticket belongs to one Project and has one User as an assignee.
   - A Ticket has many Comments.
   - A Comment belongs to one Ticket and has one User as the author.

## Phase 2: User Entity Enhancements (Google Gemini Pro)
Update the `User` entity in `com.att.tdp.issueflow.entities` to include the following new fields:
1. `fullName`: A string field for the user's full name.
2. `role`: An Enum field restricted to `DEVELOPER` and `ADMIN`.

Create the `Role` enum class and ensure the `role` field in the `User` entity is mapped as a string in the database using `@Enumerated(EnumType.STRING)`.

## Phase 3: Repository Layer (Claude Code)
Generate the Spring Data JPA Repository interfaces for the IssueFlow project under the package `com.att.tdp.issueflow.repository`.

Please create the following interfaces extending `JpaRepository`:
1. `UserRepository`: Add methods `Optional<User> findByUsername(String username)`, `boolean existsByUsername(String username)`, and `boolean existsByEmail(String email)`.
2. `ProjectRepository`: Standard JpaRepository.
3. `TicketRepository`: Add methods to find tickets by Project ID and by Assignee ID.
4. `CommentRepository`: Add a method to find all comments by Ticket ID.

## Phase 4: Data Transfer Objects (DTOs) (Claude Code)
Please implement the Data Transfer Objects (DTOs) for the Project, Ticket, and Comment subdomains following a highly organized, modular package-by-feature pattern matching the layout used for Users. 

Create all DTOs as standard classes using Lombok `@Data`, `@Builder`, `@NoArgsConstructor`, and `@AllArgsConstructor` annotations. Apply Jakarta field validation annotations (`@NotBlank`, `@NotNull`, `@Size`, etc.) to incoming payloads.

Please structure and write the following files:

**1. Under package `com.att.tdp.issueflow.dto.ProjectDTO`:**
- `AddProjectDTO.java`: Fields for `name` (NotBlank) and `description`.
- `UpdateProjectDTO.java`: Fields for `name` (NotBlank) and `description`.
- `ProjectResponseDTO.java`: Fields for `id`, `name`, `description`, `createdAt`, and `updatedAt`.

**2. Under package `com.att.tdp.issueflow.dto.TicketDTO`:**
- `AddTicketDTO.java`: Fields for `title` (NotBlank), `description`, and `projectId` (NotNull). (Do not include assigneeId here as tickets start unassigned or handled separately).
- `UpdateTicketDTO.java`: Fields for `title` (NotBlank), `description`, and `assigneeId` (to allow setting or changing who is working on it).
- `TicketResponseDTO.java`: Fields for `id`, `title`, `description`, `projectId`, `assigneeId` (nullable), `createdAt`, and `updatedAt`.

**3. Under package `com.att.tdp.issueflow.dto.CommentDTO`:**
- `AddCommentDTO.java`: Fields for `content` (NotBlank) and `ticketId` (NotNull). (The authorId should be extracted from the authenticated user context later, so do not include it in the request payload).
- `CommentResponseDTO.java`: Fields for `id`, `content`, `ticketId`, `authorId`, `createdAt`, and `updatedAt`.

*Note: Ensure all cross-references in Response DTOs flatten out deep entity graphs into primitive IDs (e.g., using `Long projectId` or `Long assigneeId` instead of nesting the whole entity object) to keep API data representations decoupled, clean, and lightweight.*

## Phase 5: Exception Handling Infrastructure - Initial (Claude Code)
Please create a standardized exception-handling infrastructure for the IssueFlow project under the package `com.att.tdp.issueflow.exception`. 

I need two files created with the following exact behaviors:

**1. Create a utility class named `Utils.java` to format standardized JSON error responses:**
- Make the class final with a private constructor that throws an AssertionError to enforce the utility pattern.
- Implement a static method: `public static ResponseEntity<Object> buildErrorResponse(Exception e, HttpStatus status, WebRequest request)`
- The method should return a ResponseEntity containing a HashMap with these exact keys:
  - "timestamp" -> LocalDateTime.now()
  - "status" -> status.value()
  - "error" -> status.getReasonPhrase()
  - "message" -> e.getMessage()
  - "path" -> request.getDescription(false)

**2. Create a global handler class named `GlobalExceptionHandler.java`:**
- Annotate it with `@RestControllerAdvice`.
- Use a static import for `com.att.tdp.issueflow.exception.Utils.buildErrorResponse`.
- Implement `@ExceptionHandler` methods for the following exception types:
  - `ResourceNotFoundException.class` -> Returns NOT_FOUND via buildErrorResponse.
  - `BadRequestException.class` -> Returns BAD_REQUEST via buildErrorResponse.
  - `SQLException.class` -> Returns INTERNAL_SERVER_ERROR via buildErrorResponse.
  - `RuntimeException.class` -> Returns INTERNAL_SERVER_ERROR via buildErrorResponse.
  - `MethodArgumentTypeMismatchException.class` -> Returns BAD_REQUEST via buildErrorResponse with a custom message.
  - `HttpMessageNotReadableException.class` -> Returns BAD_REQUEST via buildErrorResponse with a custom message.
  - `MethodArgumentNotValidException.class` -> Manually construct a ResponseEntity for field validation errors.
  - `ConstraintViolationException.class` -> Manually construct a similar validation ResponseEntity.

Ensure all components use standard Spring Boot 3 / Jakarta web annotations and match a clean production-grade REST architecture.

## Phase 6: Exception Handling Infrastructure - Refined (Claude Code)
Please build a specialized, production-grade exception handling system for the IssueFlow project under the package `com.att.tdp.issueflow.exception`. It should follow a robust REST architecture, creating two central files:

**1. Create a utility class named `Utils.java`:**
- It must be a final class with a private constructor throwing an AssertionError.
- Implement a static helper method: `public static ResponseEntity<Object> buildErrorResponse(Exception e, HttpStatus status, WebRequest request)`
- It should map errors into a standardized JSON response body containing: `timestamp`, `status`, `error`, `message`, `path`.

**2. Create a global error coordinator named `GlobalExceptionHandler.java`:**
- Annotate it with `@RestControllerAdvice`.
- Use a static import for `com.att.tdp.issueflow.exception.Utils.buildErrorResponse`.
- Implement handlers mapping the following exception cases to precise API contracts:
  - `ResourceNotFoundException.class` -> Returns HttpStatus.NOT_FOUND (404) via buildErrorResponse.
  - `BadRequestException.class` -> Returns HttpStatus.BAD_REQUEST (400) via buildErrorResponse.
  - `HttpMessageNotReadableException.class` -> Returns HttpStatus.BAD_REQUEST (400) with a clean message.
  - `MethodArgumentTypeMismatchException.class` -> Returns HttpStatus.BAD_REQUEST (400) parsing path/query parameters dynamically.
  - `MethodArgumentNotValidException.class` -> For handling @Valid input constraints. Return a manual ResponseEntity containing an "errors" collection.
  - `ConstraintViolationException.class` -> For handling parameter constraints. Return a similar structured validation payload.
  - `Exception.class` (Generic fallback) -> Returns HttpStatus.INTERNAL_SERVER_ERROR (500) via buildErrorResponse.

Ensure all classes compile perfectly with Java 21 and Spring Boot 3 dependencies.

## Phase 7: Service Layer Implementation (Claude Code)
Please implement the Service layers for Projects, Tickets, and Comments under the package `com.att.tdp.issueflow.service`. These services must use the core entities, repositories, exceptions, and DTOs we have already set up.

Ensure you implement the following files and include these specific business constraints from the requirements:

**1. `ProjectService.java`:**
- `createProject(AddProjectDTO dto)`
- `getProjectById(Long id)`
- `getAllProjects()`
- `updateProject(Long id, UpdateProjectDTO dto)`
- `deleteProject(Long id)` (Implement soft deletion logic/checks)

**2. `TicketService.java`:**
- `createTicket(AddTicketDTO dto)`
- `getTicketById(Long id)`
- `getTicketsByProject(Long projectId)`
- `updateTicket(Long id, UpdateTicketDTO dto)`
- Advanced Feature Prep (TODO Hooks): `autoAssignUnassignedTickets()` and `checkDependenciesBeforeClose()`

**3. `CommentService.java`:**
- `addComment(AddCommentDTO dto, Long authenticatedUserId)`
- `getCommentsByTicket(Long ticketId)`
- Mentions Feature Prep (TODO Hook): Parse comment text for @username tokens.

Ensure all mapping logic between Entities and DTOs handles null safety gracefully.

## Phase 8: Domain Enhancements - Ticket Status (Claude Code)
Update the `Ticket` domain to support ticket statuses as required by the system specification:
1. Create a `Status` enum under `com.att.tdp.issueflow.entities` containing: `OPEN`, `IN_PROGRESS`, `CLOSED`.
2. Update `Ticket.java` to include a `private Status status;` field. Ensure it is annotated with `@Enumerated(EnumType.STRING)` and `@NotNull`.
3. Verify that `TicketService.java` correctly sets the initial status to `Status.OPEN` inside the ticket creation method.

## Phase 9: Domain Enhancements - Requirements Compliance (Claude Code)
Please review the `TDP_issueflow_requirements.pdf` and update our JPA Entities to strictly comply with the document. Specifically, focus on the `Ticket` entity:
1. Implement the precise `Status` enum values required: `TODO`, `IN_PROGRESS`, `IN_REVIEW`, and `DONE`.
2. Implement the `Priority` enum (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`) and the `Type` enum (`BUG`, `FEATURE`, `TECHNICAL`).
3. Add the missing `dueDate` (timestamp) and `is_overdue` (boolean) fields to support the auto-escalation feature.
4. Implement Optimistic Locking by adding a `@Version private Long version;` field to the Ticket and Comment entities to satisfy the concurrency control requirements (preventing simultaneous edits).

## Phase 10: DTO Refactoring - Requirements Compliance (Claude Code)
Please read the API table in the `README.md` and the functional requirements in `TDP_issueflow_requirements.pdf` to refactor our Data Transfer Objects (DTOs). You must ensure the payloads match exactly:
1. `AddProjectDTO`: Must include an `ownerId` field.
2. `AddTicketDTO`: Must accept `status`, `priority`, `type`, and an optional `assigneeId` right at creation.
3. `AddCommentDTO`: Must explicitly require the `authorId` in the JSON body.

Audit all other DTOs and ensure no required properties from the documentation are missing.
"""