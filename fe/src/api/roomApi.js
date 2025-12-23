import axiosClient from './axiosClient';

const roomApi = {
  // GET /api/rooms
  getAllRooms: (params) => {
    return axiosClient.get('/api/rooms', { params });
  },

  // POST /api/rooms (ADMIN/DIRECTOR)
  createRoom: (payload) => {
    return axiosClient.post('/api/rooms', payload);
  },

  // PUT /api/rooms/{id} (ADMIN/DIRECTOR)
  updateRoom: (id, payload) => {
    return axiosClient.put(`/api/rooms/${id}`, payload);
  },

  // DELETE /api/rooms/{id} (ADMIN/DIRECTOR)
  deleteRoom: (id) => {
    return axiosClient.delete(`/api/rooms/${id}`);
  },
  // GET /api/rooms/{id}
  getById: (id) => {
    return axiosClient.get(`/api/rooms/${id}`);
  },

  // GET /api/rooms/code/{roomCode}
  getByCode: (roomCode) => {
    return axiosClient.get(`/api/rooms/code/${roomCode}`);
  },

  // GET /api/rooms/branch/{branchCode}
  getByBranch: (branchCode) => {
    return axiosClient.get(`/api/rooms/branch/${branchCode}`);
  },

  // GET /api/rooms/status/{status}
  getByStatus: (status) => {
    return axiosClient.get(`/api/rooms/status/${status}`);
  },

  // GET all rooms by status AVAILABLE for homepage
  getAvailableRooms: () => {
    return axiosClient.get('/api/rooms/status/AVAILABLE');
  },

  // Admin: update room status manually
  updateRoomStatus: (roomId, status) => {
    // PUT /api/rooms/{id}/status
    return axiosClient.put(`/api/rooms/${roomId}/status`, { status });
  },

  // Upload ảnh: POST /api/rooms/{roomId}/images
  uploadImages: (roomId, formData) => {
    return axiosClient.post(`/api/rooms/${roomId}/images`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  // GET /api/rooms/{roomId}/images
  listImages: (roomId) => {
    return axiosClient.get(`/api/rooms/${roomId}/images`);
  },

  // PUT /api/rooms/{id}/description
  updateDescription: (roomId, description) => {
    return axiosClient.put(`/api/rooms/${roomId}/description`, { description });
  },

  // PUT /api/rooms/{roomId}/images/{imageId}/thumbnail
  setThumbnail: (roomId, imageId) => {
    return axiosClient.put(`/api/rooms/${roomId}/images/${imageId}/thumbnail`);
  },

  // DELETE /api/rooms/{roomId}/images/{imageId}
  deleteImage: (roomId, imageId) => {
    return axiosClient.delete(`/api/rooms/${roomId}/images/${imageId}`);
  }
};

export default roomApi;