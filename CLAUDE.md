# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SecondHand Platform is a full-stack secondhand goods trading platform with auction and chat features. It consists of:
- **Frontend**: Vue 3 + Vite + Element Plus + Pinia (port 3000)
- **Backend**: Spring Boot 3.1.5 + MyBatis Plus + MySQL + Redis (port 8080)

## Commands

### Frontend (from `frontend/` directory)
```bash
npm run dev      # Start dev server on http://localhost:3000
npm run build    # Production build
npm run preview  # Preview production build
```

### Backend (from `backend/` directory)
```bash
mvn spring-boot:run                    # Start Spring Boot server
mvn clean package                      # Build JAR (target/secondhand-backend-1.0.0.jar)
mvn test                               # Run tests
```

## Architecture

### Frontend Structure
- `src/views/` - Page components (Home, GoodsDetail, Chat, Orders, etc.)
- `src/components/` - Reusable components (GoodsCard, OrderList, AiConsultant)
- `src/api/` - API modules using axios (request.js handles auth token injection)
- `src/store/` - Pinia stores (user.js, chat.js)
- `src/router/` - Vue Router with auth guards (requiresAuth meta)
- `src/layouts/` - Layout components (MainLayout.vue)

### Backend Structure
- `controller/` - REST controllers (AuthController, GoodsController, ChatController, etc.)
- `service/` - Business logic services including AiPricingService (DeepSeek API integration)
- `entity/` - JPA entities (Goods, User, Order, Auction, ChatMessage, etc.)
- `mapper/` - MyBatis Plus mappers
- `dto/` - Data Transfer Objects including unified `Result<T>` response wrapper
- `config/` - Spring config (SecurityConfig, WebSocketConfig)
- `security/` - JWT authentication filter
- `exception/` - GlobalExceptionHandler for unified error handling

### Key Integrations
- **Authentication**: JWT tokens stored in localStorage, Bearer token in Authorization header
- **WebSocket**: STOMP over SockJS for real-time chat (`/api/ws` endpoint)
- **AI Pricing**: DeepSeek API integration for secondhand item price estimation

### API Response Format
All APIs return `Result<T>` with structure:
```json
{ "code": 200, "message": "success", "data": {...} }
```
Frontend axios interceptor handles 401 (auto-logout) and error message extraction.

### Database
MySQL database `secondhand_db` on port 3307. See `secondhand_db.sql` for schema.

### Goods Status Codes
- 0: Pending review (待审核)
- 1: On sale (在售)
- 2: Sold (已售)
- 3: Delisted (下架)
- 4: In auction (拍卖中)
- 5: Violation (违规)
- 6: Violation-pending review (违规-待审核)
