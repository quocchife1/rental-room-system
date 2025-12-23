import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import authApi from '../../api/authApi';

export const loginUser = createAsyncThunk('auth/login', async (payload, thunkAPI) => {
  try {
    const response = await authApi.login(payload);
    
    // Backend trả về: { data: { accessToken, tokenType, id, username, fullName, email, phoneNumber, address, role } }
    // Giả sử axiosClient của bạn đã intercept và trả về phần 'data' bên trong ApiResponseDto
    // Nếu axiosClient trả nguyên response, hãy dùng response.data.data
    
    const { accessToken, tokenType, ...userData } = response; 
    
    localStorage.setItem('token', accessToken);
    localStorage.setItem('user', JSON.stringify(userData)); // Lưu full info
    
    return { token: accessToken, user: userData };
  } catch (error) {
    const message = error.response?.data?.message || error.message || 'Đăng nhập thất bại';
    return thunkAPI.rejectWithValue(message);
  }
});

const userFromStorage = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;
const tokenFromStorage = localStorage.getItem('token') || null;

const initialState = {
  user: userFromStorage,
  token: tokenFromStorage,
  loading: false,
  error: null,
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    logout: (state) => {
      state.user = null;
      state.token = null;
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    },
    updateUserInfo: (state, action) => {
      state.user = { ...state.user, ...action.payload };
      localStorage.setItem('user', JSON.stringify(state.user));
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(loginUser.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.loading = false;
        state.token = action.payload.token;
        state.user = action.payload.user;
        state.error = null;
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  }
});

export const { logout, updateUserInfo } = authSlice.actions;
export default authSlice.reducer;