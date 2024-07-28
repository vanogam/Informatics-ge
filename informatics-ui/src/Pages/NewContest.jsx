import dayjs from 'dayjs'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import {
	Button,
	Container,
	MenuItem,
	Paper,
	Stack,
	TextField,
	Typography,
} from '@mui/material'
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import { useContext, useRef, useState } from 'react'
import { NavLink } from 'react-router-dom'
import { AxiosContext, getAxiosInstance } from '../utils/axiosInstance'
import getMessage from '../Components/lang'
export default function NewContest() {
	const [contestId, setContestId] = useState(null)
	const [contestName, setContestName] = useState(null)
	const axiosInstance = useContext(AxiosContext)

	const [value, setValue] = useState(dayjs(new Date()))
	const [showError, setShowError] = useState(false)
	const [durationType, setDurationType] = useState('Minutes')
	const [tasks, setTasks] = useState([])
	const nameRef = useRef(null)
	const durationRef = useRef(null)
	const [showNewTaskCard, setShowNewTaskCard] = useState(false)
	const durationTypes = ['Hours', 'Minutes']

	const handleAddContest = () => {
		setShowError(true);
		if (!isValid()) {
			return;
		}
		const params = {
			name: nameRef?.current.value,
			startDate: value.format('DD/MM/YYYY HH:mm'),
			durationInSeconds:
				durationType === 'Minutes'
					? durationRef?.current.value * 60
					: durationRef?.current.value * 3600,
			roomId: "1",
		}
		params["durationInSeconds"] = params["durationInSeconds"].toString()
		setContestName(nameRef?.current.value)
		axiosInstance
			.post('/create-contest', params)
			.then((res) => {setContestId(res.data.contest.id); 	axiosInstance.post(`/contest/${res.data.contest.id}/register`,{})})

	}

	const isValid = () => {
		console.log(!!nameRef.current.value)
		console.log(!!durationRef.current.value)
		console.log(value)
		return !!nameRef.current.value
				&& !!durationRef.current.value
				&& !!value
	}

	const handleSubmit = (title) => {
		setTasks((prevState) => [...prevState, title])
		setShowNewTaskCard(false)
	}

	return (
		<LocalizationProvider dateAdapter={AdapterDayjs}>
			<Container maxWidth="xs">
				<Stack gap="1rem" marginTop="2rem">
					<Paper elevation={4} sx={{ padding: '1rem' }}>
						{!contestId ? (
							<>
								<Typography variant="h5" align="center" pb="1rem">
									ახალი კონტესტი
								</Typography>
								<Stack gap="1rem" maxWidth="25rem" mx="auto">
									<TextField
										label="სახელი"
										inputRef={nameRef}
										required={true}
										error={!nameRef.current?.value && showError}
										variant="outlined"
									/>
									<DateTimePicker
										label="დაწყების დრო"
										value={value}
										onChange={setValue}
										inputFormat={'DD/MM/YYYY HH:mm'}
										renderInput={(params) => (
											<TextField variant="outlined"
																 required={true}
																 {...params}
																 error={!value && showError}/>
										)}
									/>
									<Stack direction="row" gap="1rem">
										<TextField
											label={`${getMessage('ka', 'duration')} (${getMessage('ka', durationType === 'Minutes' ? 'minuteShort' : 'hourShort')})`}
											variant="outlined"
											required={true}
											type="number"
											error={!durationRef.current?.value && showError}
											inputRef={durationRef}
											fullWidth
										/>
										<TextField
											select
											value={durationType}
											onChange={(e) => {
												setDurationType(e.target.value)
											}}
											sx={{ minWidth: 'max-content' }}
										>
											{durationTypes.map((option) => (
												<MenuItem key={option} value={option}>
													{getMessage('ka', 'DATEFORMAT_' + option)}
												</MenuItem>
											))}
										</TextField>
									</Stack>
									<Button
										onClick={handleAddContest}
										variant="contained"
										sx = {{background: '#3c324e'}}
										size="large"
									>
										{getMessage('ka', 'addContest')}
									</Button>
								</Stack>
							</>
						) : (
							<>
								<Typography variant="h5" align="center">
									{contestName}
								</Typography>
							</>
						)}
					</Paper>
					{contestId && (
						<>
							<Paper
								elevation={4}
								sx={{  padding: '1rem', marginBottom: '0.5rem' }}
							>
								<Typography
									textAlign="center"
									variant="h6"
									marginBottom="0.5rem"
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
													#{index + 1} task:{' '}
												</span>
												{tasks}
											</Typography>
										</Paper>
									))}
								</Stack>
							</Paper>
						

						<Button
							sx = {{background: '#3c324e'}}variant="contained" size="large"
							component={NavLink}
							to="/contests"
						>
							დასრულება
						</Button>
						</>
					)}
				</Stack>
			</Container>
		</LocalizationProvider>
	)
}
