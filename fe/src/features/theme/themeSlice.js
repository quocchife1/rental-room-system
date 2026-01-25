import { createSlice } from '@reduxjs/toolkit';
import { loginUser, logout } from '../auth/authSlice';

// System default theme (enforced on every login)
export const SYSTEM_DEFAULT_THEME = 'summer';

const initialState = {
  currentTheme: SYSTEM_DEFAULT_THEME, // 'christmas' | 'summer' | 'default'
  isTransitioning: false,
};

const themeSlice = createSlice({
  name: 'theme',
  initialState,
  reducers: {
    setTheme: (state, action) => {
      state.currentTheme = action.payload;
    },
    setTransitioning: (state, action) => {
      state.isTransitioning = action.payload;
    },
    resetTheme: () => initialState,
  },
  extraReducers: (builder) => {
    builder
      // Enforce system default theme on EVERY login (ignore prior in-session choice)
      .addCase(loginUser.fulfilled, (state) => {
        state.currentTheme = SYSTEM_DEFAULT_THEME;
        state.isTransitioning = false;
      })
      // Reset completely on logout
      .addCase(logout, () => initialState);
  },
});

export const { setTheme, setTransitioning, resetTheme } = themeSlice.actions;
export default themeSlice.reducer;
