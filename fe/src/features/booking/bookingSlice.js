import { createSlice } from '@reduxjs/toolkit';

const bookingSlice = createSlice({
  name: 'booking',
  initialState: { currentBooking: null, loading: false },
  reducers: {}
});

export default bookingSlice.reducer;