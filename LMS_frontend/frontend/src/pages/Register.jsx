import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../services/axiosConfig";
import "../css/Auth.css";
import { toast } from "react-toastify";

const Register = () => {

  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
  });

  const handleSubmit = async (e) => {

    e.preventDefault();

    try {

      await api.post("/auth/register", {
        username: formData.username,
        email: formData.email,
        password: formData.password,
      });

      toast.success("Registration Successful");

      navigate("/login");

    } catch (err) {

      console.error(err);

      toast.error(
        err.response?.data?.message ||
        "Registration Failed"
      );
    }
  };

  return (
    <div className="auth-container">

      <div className="auth-card">

        <h1>Log Monitoring System</h1>

        <h2>Register</h2>

        <form onSubmit={handleSubmit}>

          <input
            type="text"
            placeholder="Username"
            value={formData.username}
            onChange={(e) =>
              setFormData({
                ...formData,
                username: e.target.value,
              })
            }
            required
          />

          <input
            type="email"
            placeholder="Email"
            value={formData.email}
            onChange={(e) =>
              setFormData({
                ...formData,
                email: e.target.value,
              })
            }
            required
          />

          <input
            type="password"
            placeholder="Password"
            value={formData.password}
            onChange={(e) =>
              setFormData({
                ...formData,
                password: e.target.value,
              })
            }
            required
          />

          <button type="submit">
            Register
          </button>

        </form>

        <p>
          Already have an account?{" "}
          <Link to="/login">
            Login
          </Link>
        </p>

      </div>

    </div>
  );
};

export default Register;