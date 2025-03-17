import { Routes, Route, Navigate } from 'react-router-dom'
import Login from './pages/Login'
import SignUp from './pages/SignUp'
import ProfileManagement from './pages/ProfileManagement'
import Home from './pages/Home'

export default function App() {

  return (
    <>
      <Routes>
        <Route path="/" element={<Home/>} />
        <Route path="/log-in" element={<Login/>} />
        <Route path="/sign-up" element={<SignUp/>} />
        <Route path="/profile" element={<ProfileManagement/>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  )
}
