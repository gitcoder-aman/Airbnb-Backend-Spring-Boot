import { Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import Home from './pages/Home';
import HotelDetail from './pages/HotelDetail';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Profile from './pages/Profile';
import Checkout from './pages/Checkout';
import MyBookings from './pages/MyBookings';
import AdminLayout from './pages/admin/AdminLayout';
import Dashboard from './pages/admin/Dashboard';
import HotelForm from './pages/admin/HotelForm';
import HotelManage from './pages/admin/HotelManage';
import ManageRooms from './pages/admin/ManageRooms';
import InventoryPage from './pages/admin/InventoryPage';
import HotelBookings from './pages/admin/HotelBookings';
import AllBookings from './pages/admin/AllBookings';
import AIAssistant from './pages/AIAssistant';
import PaymentResult from './pages/PaymentResult';

export default function App() {
  return (
    <div className="app">
      <Navbar />
      <main className="main">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/hotels/:hotelId" element={<HotelDetail />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <Profile />
              </ProtectedRoute>
            }
          />
          <Route
            path="/checkout"
            element={
              <ProtectedRoute>
                <Checkout />
              </ProtectedRoute>
            }
          />
          <Route
            path="/my-bookings"
            element={
              <ProtectedRoute>
                <MyBookings />
              </ProtectedRoute>
            }
          />
          <Route path="/payments/success" element={<PaymentResult ok />} />
          <Route path="/payments/failure" element={<PaymentResult ok={false} />} />
          <Route path="/ai-assistant" element={<AIAssistant />} />
          <Route
            path="/admin"
            element={
              <ProtectedRoute requireManager>
                <AdminLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Dashboard />} />
            <Route path="bookings" element={<AllBookings />} />
            <Route path="hotels/new" element={<HotelForm />} />
            <Route path="hotels/:hotelId/edit" element={<HotelForm />} />
            <Route path="hotels/:hotelId" element={<HotelManage />} />
            <Route path="hotels/:hotelId/rooms" element={<ManageRooms />} />
            <Route path="hotels/:hotelId/inventory" element={<InventoryPage />} />
            <Route path="hotels/:hotelId/bookings" element={<HotelBookings />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  );
}