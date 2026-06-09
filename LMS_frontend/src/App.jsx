import {BrowserRouter, Routes, Route, Navigate,} from "react-router-dom";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import ProtectedRoute from "./components/ProtectedRoute";

import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";


function App() {

  return (
    <>
    <BrowserRouter>

      <Routes>

         <Route
          path="/"
          element={<Navigate to="/login" replace />}
        />

        <Route
          path="/login"
          element={<Login />}
        />

        <Route
          path="/register"
          element={<Register />}
        />

        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />

        <Route
          path="*"
          element={<Login />}
        />

      </Routes>

    </BrowserRouter>
    
    <ToastContainer
        position="top-right"
        autoClose={3000}
        theme="dark"
    />
   </>   
  );
}

export default App;