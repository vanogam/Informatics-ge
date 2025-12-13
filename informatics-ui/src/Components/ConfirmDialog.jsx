import {
	Button,
	Dialog,
	DialogTitle,
	DialogContent,
	DialogActions,
	Typography,
	Box,
} from '@mui/material'
import { Info, Warning } from '@mui/icons-material'
import { blue, orange } from '@mui/material/colors'

function ConfirmDialog({
	open,
	message,
	type = 'info',
	onConfirm,
	onCancel = () => {},
	title,
}) {
	const isWarning = type === 'warning'
	const iconColor = isWarning ? orange[600] : blue[600]
	const Icon = isWarning ? Warning : Info
	const confirmButtonColor = isWarning ? 'error' : 'primary'

	return (
		<Dialog
			open={open}
			onClose={onCancel}
			maxWidth="sm"
			fullWidth
		>
			<DialogTitle>
				<Box display="flex" alignItems="center" gap={1}>
					<Icon sx={{ color: iconColor }} />
					<Typography variant="h6" component="span">
						{title || (isWarning ? 'Warning' : 'Confirmation')}
					</Typography>
				</Box>
			</DialogTitle>
			<DialogContent>
				<Typography variant="body1">{message}</Typography>
			</DialogContent>
			<DialogActions>
				<Button onClick={onCancel} color="inherit">
					Cancel
				</Button>
				<Button
					onClick={onConfirm}
					variant="contained"
					color={confirmButtonColor}
					autoFocus
				>
					Confirm
				</Button>
			</DialogActions>
		</Dialog>
	)
}

export default ConfirmDialog

