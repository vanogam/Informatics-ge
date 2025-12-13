import { createContext, useContext, useState } from 'react'
import ConfirmDialog from '../Components/ConfirmDialog'

const ConfirmDialogContext = createContext()

export function ConfirmDialogProvider({ children }) {
	const [dialogState, setDialogState] = useState({
		open: false,
		message: '',
		type: 'info',
		onConfirm: () => {},
		onCancel: () => {},
		title: null,
	})

	const showConfirmDialog = ({
		message,
		type = 'info',
		onConfirm,
		onCancel = () => {},
		title,
	}) => {
		setDialogState({
			open: true,
			message,
			type,
			onConfirm: () => {
				setDialogState((prev) => ({ ...prev, open: false }))
				onConfirm()
			},
			onCancel: () => {
				setDialogState((prev) => ({ ...prev, open: false }))
				onCancel()
			},
			title,
		})
	}

	const hideConfirmDialog = () => {
		setDialogState((prev) => ({ ...prev, open: false }))
	}

	return (
		<ConfirmDialogContext.Provider value={{ showConfirmDialog, hideConfirmDialog }}>
			{children}
			<ConfirmDialog
				open={dialogState.open}
				message={dialogState.message}
				type={dialogState.type}
				onConfirm={dialogState.onConfirm}
				onCancel={dialogState.onCancel}
				title={dialogState.title}
			/>
		</ConfirmDialogContext.Provider>
	)
}

export function useConfirmDialog() {
	const context = useContext(ConfirmDialogContext)
	if (!context) {
		throw new Error('useConfirmDialog must be used within ConfirmDialogProvider')
	}
	return context
}

