import {
	Container,
	Typography,
	Table,
	TableBody,
	TableCell,
	TableHead,
	TableRow,
	TableContainer,
	Paper,
	Chip,
	CircularProgress,
	Box,
	Button,
	TextField,
	Dialog,
	DialogTitle,
	DialogContent,
	DialogActions,
	IconButton,
} from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'
import { useState, useEffect, useContext } from 'react'
import { useNavigate } from 'react-router-dom'
import { AuthContext } from '../store/authentication'
import { AxiosContext } from '../utils/axiosInstance'

function formatUptime(seconds) {
	if (!seconds) return '0s'
	const days = Math.floor(seconds / 86400)
	const hours = Math.floor((seconds % 86400) / 3600)
	const minutes = Math.floor((seconds % 3600) / 60)
	const secs = seconds % 60
	
	const parts = []
	if (days > 0) parts.push(`${days}d`)
	if (hours > 0) parts.push(`${hours}h`)
	if (minutes > 0) parts.push(`${minutes}m`)
	if (secs > 0 || parts.length === 0) parts.push(`${secs}s`)
	
	return parts.join(' ')
}

function formatDate(dateString) {
	if (!dateString) return 'N/A'
	const date = new Date(dateString)
	return date.toLocaleString('ka-GE', {
		year: 'numeric',
		month: '2-digit',
		day: '2-digit',
		hour: '2-digit',
		minute: '2-digit',
		second: '2-digit',
	})
}

export default function AdminPanel() {
	const axiosInstance = useContext(AxiosContext)
	const authContext = useContext(AuthContext)
	const navigate = useNavigate()
	const [workers, setWorkers] = useState([])
	const [loading, setLoading] = useState(true)
	const [error, setError] = useState(null)
	const [createDialogOpen, setCreateDialogOpen] = useState(false)
	const [workerCount, setWorkerCount] = useState(1)
	const [actionLoading, setActionLoading] = useState(false)

	const fetchWorkers = () => {
		axiosInstance
			.get('/admin/workers')
			.then((response) => {
				if (response.data && response.data.workers) {
					setWorkers(response.data.workers)
					setError(null)
				} else {
					setError('Invalid response format')
				}
				setLoading(false)
			})
			.catch((error) => {
				console.error('Error fetching workers:', error)
				if (error.response?.status === 403) {
					setError('You do not have permission to access this page')
					navigate('/')
				} else if (error.response?.status === 401) {
					setError('Please log in to access this page')
					navigate('/')
				} else {
					setError('Failed to load workers. Please try again later.')
				}
				setLoading(false)
			})
	}

	const handleCreateWorkers = () => {
		if (workerCount < 1) {
			setError('Count must be at least 1')
			return
		}
		setActionLoading(true)
		axiosInstance
			.post('/admin/workers', { count: workerCount })
			.then((response) => {
				setCreateDialogOpen(false)
				setWorkerCount(1)
				fetchWorkers()
				setActionLoading(false)
			})
			.catch((error) => {
				console.error('Error creating workers:', error)
				setError(error.response?.data?.message || 'Failed to create workers')
				setActionLoading(false)
			})
	}

	const handleStopAllWorkers = () => {
		if (!window.confirm('ნამდვილად გსურთ ყველა ვორკერის გაჩერება?')) {
			return
		}
		setActionLoading(true)
		axiosInstance
			.delete('/admin/workers')
			.then(() => {
				fetchWorkers()
				setActionLoading(false)
			})
			.catch((error) => {
				console.error('Error stopping workers:', error)
				setError(error.response?.data?.message || 'Failed to stop workers')
				setActionLoading(false)
			})
	}

	const handleDeleteWorker = (workerId) => {
		if (!window.confirm(`ნამდვილად გსურთ ვორკერის ${workerId} წაშლა?`)) {
			return
		}
		setActionLoading(true)
		axiosInstance
			.delete(`/admin/workers/${workerId}`)
			.then(() => {
				fetchWorkers()
				setActionLoading(false)
			})
			.catch((error) => {
				console.error('Error deleting worker:', error)
				setError(error.response?.data?.message || 'Failed to delete worker')
				setActionLoading(false)
			})
	}

	useEffect(() => {
		// Wait for auth context to load before checking
		if (authContext.authLoading) {
			return
		}

		// Check if user is admin
		if (!authContext.isLoggedIn || !authContext.role || !authContext.role.includes('ADMIN')) {
			navigate('/')
			return
		}

		fetchWorkers()
		
		// Auto-refresh every 5 seconds
		const interval = setInterval(() => {
			fetchWorkers()
		}, 5000)

		return () => clearInterval(interval)
	}, [authContext.isLoggedIn, authContext.role, authContext.authLoading, navigate])

	if (authContext.authLoading || loading) {
		return (
			<Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
				<CircularProgress />
			</Box>
		)
	}

	if (error) {
		return (
			<Container maxWidth="lg">
				<Typography variant="h6" color="error" align="center" mt="2rem">
					{error}
				</Typography>
			</Container>
		)
	}

	return (
		<main>
			<Typography
				variant="h6"
				fontWeight="bold"
				mt="1rem"
				align="center"
				sx={{ color: '#452c54', fontWeight: 'bold' }}
			>
				ადმინისტრატორის პანელი
			</Typography>
			<Typography
				paragraph
				align="center"
				pt="0.5rem"
				pb="1rem"
				borderBottom="2px dashed #aaa"
				sx={{ color: '#281d2e' }}
			>
				ვორკერების სტატუსი და მეტრიკები
			</Typography>
			<Container maxWidth="lg">
				<Box display="flex" justifyContent="space-between" alignItems="center" mb={2} mt={2}>
					<Button
						variant="contained"
						color="primary"
						onClick={() => setCreateDialogOpen(true)}
						disabled={actionLoading}
						sx={{ backgroundColor: '#2f2d47' }}
					>
						ვორკერების დამატება
					</Button>
					<Button
						variant="contained"
						color="error"
						onClick={handleStopAllWorkers}
						disabled={actionLoading || workers.length === 0}
					>
						ყველა ვორკერის გაჩერება
					</Button>
				</Box>
				<TableContainer component={Paper} sx={{ marginInline: 'auto', mt: 2 }}>
					<Table sx={{ marginX: 'auto' }}>
						<TableHead>
							<TableRow>
								<TableCell>ვორკერის ID</TableCell>
								<TableCell align="right">სტატუსი</TableCell>
								<TableCell align="right">ბოლო Heartbeat</TableCell>
								<TableCell align="right">Uptime</TableCell>
								<TableCell align="right">დამუშავებული Jobs</TableCell>
								<TableCell align="right">მოქმედებები</TableCell>
							</TableRow>
						</TableHead>
						<TableBody>
							{workers.length === 0 ? (
								<TableRow>
									<TableCell colSpan={6} align="center">
										<Typography variant="body2" color="text.secondary">
											ვორკერები არ მოიძებნა
										</Typography>
									</TableCell>
								</TableRow>
							) : (
								workers.map((worker) => (
									<TableRow
										key={worker.workerId}
										sx={{
											'&:last-child td, &:last-child th': { border: 0 },
											'&:hover': { backgroundColor: '#eee' },
										}}
									>
										<TableCell component="th" scope="row">
											{worker.workerId}
										</TableCell>
										<TableCell align="right">
											<Chip
												label={
													worker.status === 'ONLINE' ? 'ონლაინ' :
													worker.status === 'WORKING' ? 'მუშაობს' :
													'ოფლაინ'
												}
												color={
													worker.status === 'ONLINE' ? 'success' :
													worker.status === 'WORKING' ? 'warning' :
													'error'
												}
												size="small"
											/>
										</TableCell>
										<TableCell align="right">
											{formatDate(worker.lastHeartbeat)}
										</TableCell>
										<TableCell align="right">
											{formatUptime(worker.uptimeSeconds)}
										</TableCell>
										<TableCell align="right">
											{worker.jobsProcessed || 0}
										</TableCell>
										<TableCell align="right">
											<IconButton
												color="error"
												size="small"
												onClick={() => handleDeleteWorker(worker.workerId)}
												disabled={actionLoading}
											>
												<DeleteIcon />
											</IconButton>
										</TableCell>
									</TableRow>
								))
							)}
						</TableBody>
					</Table>
				</TableContainer>
			</Container>
			<Dialog open={createDialogOpen} onClose={() => setCreateDialogOpen(false)}>
				<DialogTitle>ვორკერების დამატება</DialogTitle>
				<DialogContent>
					<TextField
						autoFocus
						margin="dense"
						label="ვორკერების რაოდენობა"
						type="number"
						fullWidth
						variant="standard"
						value={workerCount}
						onChange={(e) => setWorkerCount(Math.max(1, parseInt(e.target.value) || 1))}
						inputProps={{ min: 1 }}
					/>
				</DialogContent>
				<DialogActions>
					<Button onClick={() => setCreateDialogOpen(false)} disabled={actionLoading}>
						გაუქმება
					</Button>
					<Button onClick={handleCreateWorkers} disabled={actionLoading} variant="contained">
						დამატება
					</Button>
				</DialogActions>
			</Dialog>
		</main>
	)
}

