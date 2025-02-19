import { Routes, Route, Navigate } from 'react-router-dom'
import SearchBar from './components/SearchBar'
import Login from './pages/Login'
import SignUp from './pages/SignUp'

export default function App() {

  return (
    <>
      <Routes>
        <Route path="/" element={<SearchBar/>} />
        <Route path="/log-in" element={<Login/>} />
        <Route path="/sign-up" element={<SignUp/>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  )
}
