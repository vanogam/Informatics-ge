import { createContext, useCallback, useEffect, useState } from 'react'

export const AuthContext = createContext({
	isLoggedIn: false,
	username: '',
	login: () => {},
	logout: () => {},
})

export const AuthContextProvider = (props) => {
	const [isLoggedIn, setIsLoggedIn] = useState(false)
	const [username, setUsername] = useState('')
	useEffect(() => {
		if (localStorage.getItem('username')) {
			setIsLoggedIn(true)
			setUsername(localStorage.getItem('username'))
		} else {
			setIsLoggedIn(false)
			setUsername('')
		}
	}, [])

	const logoutHandler = useCallback(() => {
		localStorage.removeItem('username')
		localStorage.removeItem('roles')
		setIsLoggedIn(false)
		setUsername('')
	}, [])

	const loginHandler = useCallback(({ username, roles }) => {
		localStorage.setItem('username', username)
		localStorage.setItem('roles', roles)
		setIsLoggedIn(true)
		setUsername(localStorage.getItem('username'))
	}, [])

	const contextValue = {
		isLoggedIn: isLoggedIn,
		username: isLoggedIn ? localStorage.getItem('username') : '',
		login: loginHandler,
		logout: logoutHandler,
	}

	return (
		<AuthContext.Provider value={contextValue}>
			{props.children}
		</AuthContext.Provider>
	)
}
