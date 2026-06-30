# WorkforceX

AI-powered workforce recruitment platform for blue-collar and semi-skilled workers (security guards, drivers, housekeeping, hospitality, construction, technicians, etc.)

## Structure
- `backend/` — Spring Boot 3.5 (Java 21), Maven, H2 (dev) → PostgreSQL/Supabase (prod)
- `android/` — Android app (Java), built after backend APIs are ready

## Spiral Development
Following spiral methodology. Spiral 1 goal:
Employer creates a Job → System ranks Workers → Employer sees ranked candidates.

## Spiral 1 Status
- [x] Milestone 1: Spring Boot project skeleton (Web, Data JPA, H2, Security, Validation, Lombok)
- [x] Milestone 2: Package structure
- [x] Milestone 3: Role enum
- [x] Milestone 4: User entity
- [x] Milestone 5: User repository
- [x] Milestone 6: Registration API
- [x] Milestone 7: Login API
- [x] Milestone 8: JWT authentication
- [x] Milestone 9: Worker profile
- [x] Milestone 10: Employer profile
- [x] Milestone 11: Job module
- [ ] Milestone 12: Matching engine
- [ ] Android app (parallel track)
