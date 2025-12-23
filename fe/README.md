FE scaffold (Vite + React + Tailwind + Redux)

Quick start:
1. cd fe
2. npm install
3. npm run dev

Notes:
- The frontend expects backend APIs at the same origin (`/api/...`). When developing, you can set a proxy in `vite.config.js` or use full backend URL in axios requests.
- This scaffold includes a minimal `DamageReportForm` that calls `/api/damage-reports` with multipart/form-data.

FE structure added (high level):
- `src/components/` common UI components (Sidebar, LoginForm, RegisterForm, RoomList...)
- `src/pages/` pages for each subsystem: Dashboard, Rooms, Booking, DamageReportFlow, Billing, Maintenance, Checkout, Partner, Reports, Audit, Auth

Notes for development:
- Install dependencies: `npm install` (includes `react-router-dom`).
- The FE expects backend API under `/api`. During development you can set a proxy in Vite config or use full backend URL in axios requests.
- Run FE: `npm run dev` (default port 3000).

Next steps already scaffolded:
- Routing and placeholder pages for all subsystems listed in the spec.
- Damage report flow UI is scaffolded (client-side). Backend endpoints remain authoritative for saving data.
