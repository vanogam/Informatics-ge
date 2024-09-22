import dayjs from 'dayjs'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import {
	Button,
	Container,
	Paper,
	Stack,
	TextField,
	Typography,
	Box,
	FormGroup,
	FormControlLabel,
	Checkbox,
} from '@mui/material'
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import { useContext, useEffect, useRef, useState } from 'react'
import { useParams } from 'react-router-dom'
import { NavLink } from 'react-router-dom'
import { AxiosContext, getAxiosInstance } from '../utils/axiosInstance'
import getMessage from '../Components/lang'

export default function EditContest() {
	const axiosInstance = useContext(AxiosContext)
	const params = useParams()
	const [name, setName] = useState(null)
	const [saved, setSaved] = useState(null)
	const [tasks, setTasks] = useState([])
	const [showNewTaskCard, setShowNewTaskCard] = useState(false)
	const [contestData, setContestData] = useState({
		contestName: ' ',
		startDate: dayjs('2015-08-18T21:11:54'),
		duration: 0,
		archive: false,
		autoArchive: false,
	})

	useEffect(() => {
		axiosInstance.get(`/contest/${params.contest_id}`).then((response) => {
			const contest = response.data
			setName(contest.name)
			setTasks(contest.tasks)
			setContestData((prevData) => ({
				...prevData,
				contestName: contest.name,
				startDate: dayjs(contest.startDate),
				duration: contest.durationInSeconds / 60,
				archive: contest.upsolving,
				autoArchive: contest.upsolvingAfterFinish,
			}))
		})
	}, [axiosInstance, params.contest_id])

	const handleAddContest = () => {
		const params = {
			name: contestData.contestName,
			startDate: contestData.startDate.format('DD/MM/YYYY HH:mm'),
			durationInSeconds: contestData.duration * 60,
			roomId: '1',
			contestId: parseInt(params.contest_id),
			upsolving: contestData.archive,
			upsolvingAfterFinish: contestData.autoArchive,
		}
		params['durationInSeconds'] = params['durationInSeconds'].toString()
		console.log(params)
		axiosInstance.post('/create-contest', params).then(() => {
			setSaved(true)
		})
	}

	const handleSubmit = (title) => {
		setTasks((prevTasks) => [...prevTasks, title])
		setShowNewTaskCard(false)
	}

	const handleChange = (event) => {
		setContestData((prevData) => ({ ...prevData, archive: event.target.checked }))
	}

	const handleChange2 = (event) => {
		setContestData((prevData) => ({ ...prevData, autoArchive: event.target.checked }))
	}

	return (
		<LocalizationProvider dateAdapter={AdapterDayjs}>
			<Container maxWidth='xs'>
				<Stack gap='1rem' marginTop='2rem'>
					<Paper elevation={4} sx={{ padding: '1rem' }}>
						{!saved ? (
							<>
								<Typography variant='h5' align='center' pb='1rem'>
									{name}
								</Typography>
								<Stack gap='1rem' maxWidth='25rem' mx='auto'>
									<TextField
										label='Contest Name'
										value={contestData.contestName}
										onChange={(e) =>
											setContestData((prevData) => ({
												...prevData,
												contestName: e.target.value,
											}))
										}
										variant='outlined'
									/>
									<Box
										sx={{
											display: 'flex',
											flexDirection: 'row',
										}}
									>
										<FormGroup>
											<FormControlLabel
												control={
													<Checkbox
														checked={contestData.archive}
														onChange={handleChange}
														inputProps={{ 'aria-label': 'controlled' }}
													/>
												}
												label='დაარქივება'
											/>
										</FormGroup>
										<FormGroup>
											<FormControlLabel
												control={
													<Checkbox
														checked={contestData.autoArchive}
														onChange={handleChange2}
														inputProps={{ 'aria-label': 'controlled' }}
													/>
												}
												label='ავტომატური დაარქივება'
											/>
										</FormGroup>
									</Box>
									<DateTimePicker
										label='Start Date'
										value={contestData.startDate}
										onChange={(date) =>
											setContestData((prevData) => ({ ...prevData, startDate: date }))
										}
										inputFormat={'DD/MM/YYYY HH:mm'}
										renderInput={(params) => <TextField variant='outlined' {...params} />}
									/>
									<Stack direction='row' gap='1rem'>
										<TextField
											label='ხანგრძლივობა (წთ)'
											variant='outlined'
											type='number'
											value={contestData.duration}
											onChange={(e) =>
												setContestData((prevData) => ({
													...prevData,
													duration: e.target.value,
												}))
											}
											fullWidth
										/>
									</Stack>
									<Button
										onClick={handleAddContest}
										variant='contained'
										sx={{ background: '#3c324e' }}
										size='large'
									>
										რედაქტირება
									</Button>
								</Stack>
							</>
						) : (
							<>
								<Typography variant='h4' align='center'>
									{contestData.contestName}
								</Typography>
							</>
						)}
					</Paper>
					<Paper
						elevation={4}
						sx={{ padding: '1rem', marginBottom: '0.5rem' }}
					>
						<Typography
							textAlign='center'
							variant='h6'
							marginBottom='0.5rem'
						>
							ამოცანები
						</Typography>
						<Stack>
							{tasks?.map((task, index) => (
								<Paper
									elevation={4}
									sx={{ padding: '1rem', marginBottom: '0.5rem' }}
									key={task}
								>
									<Typography>
												<span style={{ fontWeight: 700 }}>
													<Box sx={{ display: 'flex', justifyContent: 'space-between', width: '100%' }}>
														<Box>
															#{index + 1} : {task.title.KA}
														</Box>
														<Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
															<Button component={NavLink}
																			variant='contained'
																			sx={{ background: '#3c324e' }}
																			to={`/task/${task.id}`}
																			target='_blank'>
																{getMessage('ka', 'edit')}
															</Button>
														</Box>
													</Box>
												</span>
										{/*{tasks}*/}
									</Typography>
								</Paper>
							))}
							<Paper elevation={4} sx={{ padding: '1rem' }}>
								<Button
									fullWidth
									component={NavLink}
									variant='contained'
									sx={{ background: '#3c324e' }}
									to={`/contest/${params.contest_id}/add-task`}
									target='_blank'
								>
									{getMessage('ka', 'addProblem')}
								</Button>
							</Paper>
						</Stack>
					</Paper>
					<Button
						sx={{ background: '#3c324e' }} variant='contained' size='large'
						component={NavLink}
						to='/contests'
					>
						დასრულება
					</Button>
				</Stack>
			</Container>
		</LocalizationProvider>
	)
}
