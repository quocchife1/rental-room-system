import { configureStore } from '@reduxjs/toolkit';
import authReducer from '../features/auth/authSlice';
import roomsReducer from '../features/rooms/roomsSlice';
import invoicesReducer from '../features/invoices/invoicesSlice';
import bookingReducer from '../features/booking/bookingSlice';
import themeReducer from '../features/theme/themeSlice';

export default configureStore({
  reducer: {
    auth: authReducer,
    rooms: roomsReducer,
    invoices: invoicesReducer,
    booking: bookingReducer,
    theme: themeReducer,
  },
  // Tắt serializableCheck nếu gặp cảnh báo về non-serializable data (tùy chọn)
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: false,
    }),
});