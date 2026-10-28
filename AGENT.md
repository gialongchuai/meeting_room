# AGENT.md — Service Coding Rules

These rules apply to every task in this repository. Read them fully before writing code.
If a rule here conflicts with a specification document, **this file wins on coding conventions**,
and the specification document wins on business behavior.

## 0. Hard Rules (never violate)

1. **Do NOT run any command** for build, test, or run after finishing the work.
2. **Do NOT write unit tests** (or integration tests).
3. **Format code to the standard style** (4-space indentation, no unused imports, no trailing whitespace, one blank line between members, lines ≤ 120 characters).
4. **Never hardcode secrets** (passwords, JWT secrets, API keys). Read them from environment variables / configuration.
5. **Never expose JPA entities** through REST APIs.

---

# Java Backend Development Guidelines

Implement all Java backend code according to the following engineering standards and architectural principles.

## 1. Reuse Existing Code

Before creating any new method, class, or utility:

* Search the current module/codebase to determine whether equivalent or similar functionality already exists.
* Reuse or extend existing implementations instead of duplicating logic.
* If the same or similar logic is required by multiple services, extract it into a reusable utility, shared component, or appropriate abstraction.
* Avoid duplicate code and unnecessary helper methods.

## 2. Apply Design Patterns

Prefer established design patterns when they improve maintainability, extensibility, and readability.

### Strategy + Factory

Use **Strategy + Factory** when there are two or more implementations representing different behaviors, business types, processing flows, or object categories.

Instead of large conditional blocks (`if/else`, `switch`, type-based branching), define separate strategies and select the right implementation through a Factory.

### Other Patterns

Apply when appropriate: Builder, Template Method, Adapter, Facade, Chain of Responsibility, Specification, Observer, Repository.

**Do not introduce patterns unnecessarily.** A pattern must simplify the architecture, not add complexity.
Avoid scattered business logic and code structures that are hard to maintain or extend.

## 3. Follow OOP Principles

Prefer Object-Oriented Programming principles:

* Encapsulation
* Abstraction
* Inheritance
* Polymorphism
* Interfaces and abstract classes

Rules:

* Classes sharing common characteristics or behavior must use an appropriate abstraction.
* Prefer polymorphism over large `if/else` or `switch` blocks when behavior varies by object type.
* Follow SOLID principles whenever applicable.

## 4. DTO Design

* Do not expose JPA entities directly through REST APIs.
* Create DTOs appropriate to each controller/API use case.
* Separate DTOs by responsibility, for example:
  * `CreateXxxRequest`
  * `UpdateXxxRequest`
  * `XxxResponse`
  * `XxxDetailResponse`
  * `XxxListResponse`
  * `XxxLookupRequest`
  * `XxxLookupResponse`
* Request and response DTOs contain **only the fields required by that specific API**.
* Do not create one oversized DTO and reuse it for unrelated endpoints.
* Validate request DTOs with Jakarta Bean Validation annotations.

## 5. Pagination

All APIs returning collections or lists **must support pagination**, unless there is a clear technical or business reason not to (document that reason in code comments and README).

* Default: `page = 0`, `size = 10`. Enforce a maximum size (e.g. 100).
* Use Spring Data pagination: `Pageable`, `PageRequest`, `Page<T>`.
* Do not load the entire table into memory and paginate manually.

## 6. Entity ↔ DTO Mapping, Localization, Date/Time

**Mapping**

* Use **MapStruct** for Entity → DTO and DTO → Entity conversion.
* Do not manually map fields inside services/controllers when MapStruct can do it.
* If MapStruct is not configured, add the dependency and annotation processors (including `lombok-mapstruct-binding` when used with Lombok).
* Create dedicated mapper interfaces, e.g. `CustomerMapper`, `AccountMapper`, `TransactionMapper`.
* Keep mapping logic out of controllers and business services.

**Localization**

* Fields that carry **content or title** (display text managed by the system) must be localized in **`{vi, en}`**.
* Store as separate columns (`xxx_vi`, `xxx_en`) and expose as an object `{ "vi": "...", "en": "..." }`.
* Error/validation messages are resolved by the `Accept-Language` header (`vi` default, `en`) from message bundles.
* Implement the `{vi, en}` conversion **once** (shared type/mapper method) and reuse it everywhere.

**Date/Time**

* All date-time fields use **`Long`** (epoch milliseconds, UTC) in entities, DTOs, and database columns.
* Convert to/from `ZonedDateTime` only inside a shared time utility when business logic needs a time zone.

## 7. Lombok

Prefer **Lombok** to reduce boilerplate where appropriate:

* `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, `@NoArgsConstructor`, `@AllArgsConstructor`

Rules:

* Use constructor injection via `@RequiredArgsConstructor`.
* **Never** use field injection with `@Autowired`.
* On JPA entities, avoid `@Data` and auto-generated `equals/hashCode/toString` over relationships.

## 8. Layered Architecture

Strictly follow this dependency flow:

`Controller → Service → Repository`

**Controller**

* Handle HTTP requests and responses.
* Validate request DTOs (`@Valid`).
* Delegate business operations to services.
* Must not contain business logic.
* Must not access repositories directly.

**Service**

* Implement business rules and application logic.
* Coordinate repositories, mappers, strategies, factories, and other services.
* Own the transactional boundaries (`@Transactional`; `readOnly = true` for queries).

**Repository**

* Database access only.
* Use Spring Data JPA whenever possible.
* No business logic inside repositories.

Never bypass layers (e.g. `Controller → Repository` is forbidden).

## 9. Error Handling

* Throw domain-specific exceptions (e.g. `BusinessException` with an `ErrorCode` enum) from services.
* Handle all exceptions centrally in a single `@RestControllerAdvice`.
* Return one consistent error response shape (`code`, `message`, `timestamp`, `path`, `details`).
* Use correct HTTP status codes (400 validation, 401 unauthenticated, 403 forbidden, 404 not found, 409 conflict).
* Never leak stack traces or internal details to clients.

## 10. Security & Configuration

* Check authorization (role **and** ownership) in the service layer, not only at the controller.
* Never log passwords, tokens, or other sensitive data.
* Externalize configuration (`application.yml` + environment variables); no environment-specific values in code.
* Provide an `.env.example` for required variables.

## 11. General Code Quality

When implementing or modifying code:

1. Inspect the existing project structure first.
2. Reuse existing components whenever possible.
3. Follow the project's existing naming conventions and package structure.
4. Keep classes and methods focused on a single responsibility.
5. Avoid duplicated business logic.
6. Avoid unnecessarily large methods and classes.
7. Prefer composition and polymorphism over complex conditional logic.
8. Keep the implementation easy to maintain and extend.
9. Do not introduce unnecessary dependencies or abstractions.
10. Preserve backward compatibility unless the requested change explicitly breaks it.

## 12. Before Finishing (self-review checklist)

Review the affected code (by reading — **do not run build/test commands**) and confirm:

- [ ] No entity is exposed through any API; each endpoint has its own DTOs.
- [ ] Layering is respected (Controller → Service → Repository).
- [ ] MapStruct is used for all mapping; no manual field copying in services/controllers.
- [ ] Constructor injection only; no `@Autowired` on fields.
- [ ] Every list endpoint is paginated (default `page=0`, `size=10`) or has a documented exception.
- [ ] Date-time fields are `Long`; content/title fields are localized `{vi, en}`.
- [ ] No duplicated logic; shared logic extracted and reused.
- [ ] No `if/else`/`switch` chains that should be Strategy + Factory.
- [ ] No hardcoded secrets; configuration externalized.
- [ ] No unit tests were added and no build/test/run commands were executed.
- [ ] Code is formatted to the standard style.
