import { createContext, useCallback, useEffect, useState } from 'react'

export const AuthContext = createContext({
	isLoggedIn: false,
	login: () => {},
	logout: () => {},
})

export const AuthContextProvider = (props) => {
	const [isLoggedIn, setIsLoggedIn] = useState(false)
	useEffect(() => {
		if (localStorage.getItem('username')) {
			setIsLoggedIn(true)
		} else {
			setIsLoggedIn(false)
		}
	}, [])

	const logoutHandler = useCallback(() => {
		localStorage.removeItem('username')
		setIsLoggedIn(false)
	}, [])

	const loginHandler = useCallback((username) => {
		localStorage.setItem('username', username)
		setIsLoggedIn(true)
	}, [])

	const contextValue = {
		isLoggedIn: isLoggedIn,
		login: loginHandler,
		logout: logoutHandler,
	}

	return (
		<AuthContext.Provider value={contextValue}>
			{props.children}
		</AuthContext.Provider>
	)
}
