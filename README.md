# FRSCES - Attendance and Monitoring System

### Description
FRSCES is a JavaFX-based attendance monitoring system designed for schools. It uses QR code scanning to record student attendance and stores data in a centralized PostgreSQL database accessible by multiple devices over a local network.

### Team Members
 Andrade, Lea M.
 Figuron, Sophia Gabrielle E.
 Franco, Leahvenchiteve M. 
 Rollorata, Elebeth  M. 
 Lim, Wesley Ernest O.

### Setup Instructions

### Requirements
- Java 17
- PostgreSQL 17
- JavaFX SDK 17

### Database Setup
1. Install PostgreSQL
2. Create database: `CREATE DATABASE frsces_db;`
3. Import schema: `psql -U postgres -d frsces_db -f frsces_db.sql`

### Running the App
1. Extract the deployment package
2. Open `config.properties` and set `db.host` to the server PC's IP
3. Double click `FRSCES.exe`
