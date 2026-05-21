Google Gemini Pro model:


    ## Prompt 1
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

    ## Prompt 2
    Update the `User` entity in `com.att.tdp.issueflow.entities` to include the following new fields:
    1. `fullName`: A string field for the user's full name.
    2. `role`: An Enum field restricted to `DEVELOPER` and `ADMIN`.
    Create the `Role` enum class and ensure the `role` field in the `User` entity is mapped as a string in the database using `@Enumerated(EnumType.STRING)`.