import { createSlice } from '@reduxjs/toolkit';

const roomsSlice = createSlice({
  name: 'rooms',
  initialState: { items: [], loading: false },
  reducers: {}
});

export default roomsSlice.reducer;