import { Button, Container, MenuItem, Paper, Stack, TextField, Typography } from '@mui/material'
import { useContext, useEffect, useRef, useState } from 'react'
import NewTestCaseCard from './NewTestCaseCard'
import { AxiosContext, getAxiosInstance } from '../utils/axiosInstance'
import getMessage from './lang'
import { toast } from 'react-toastify'
import { useParams } from 'react-router-dom'

export default function NewTaskCard() {
	const pageParams = useParams();
	const [code, setCode] = useState('');
	const [titleEN, setTitleEN] = useState('');
	const [titleKA, setTitleKA] = useState('');
	const [taskType, setTaskType] = useState('BATCH');
	const [taskScoreType, setTaskScoreType] = useState('SUM');
	const [taskScoreParameter, setTaskScoreParameter] = useState('');
	const [timeLimitMillis, setTimeLimitMillis] = useState('');
	const [memoryLimitMB, setMemoryLimitMB] = useState('');
	const [inputTemplate, setInputTemplate] = useState('');
	const [outputTemplate, setOutputTemplate] = useState('');
	const [enStatement, setEnStatement] = useState(null);
	const [kaStatement, setKaStatement] = useState(null);
	const [fieldValidations, setFieldValidations] = useState({
		code: true,
		titleKA: true,
		timeLimitMillis: true,
		memoryLimitMB: true,
		inputTemplate: true,
		outputTemplate: true,
	});

	const [showNewTestCaseCard, setShowNewTestCaseCard] = useState(false)
	const [testCases, setTestCases] = useState([])
	const [taskId, setTaskId] = useState('')
	const handleTestCaseSubmit = (params) => {
		setTestCases((prevState) => [...prevState, params])
		setShowNewTestCaseCard(false)
	}
	const axiosInstance = useContext(AxiosContext)
	const taskScoreTypes = ['SUM', 'GROUP_MIN']
	const taskTypes = ['BATCH']
	useEffect(() => {
		console.log(pageParams)
		if (!!pageParams.task_id) {
			axiosInstance.get(`/task/${pageParams.task_id}`).then((response) => {
				console.log(response)
				const task = response.data
				setCode(task.code);
				setTitleEN(task.title.EN);
				setTitleKA(task.title.KA);
				setTaskType(task.taskType);
				setTaskScoreType(task.taskScoreType);
				setTaskScoreParameter(task.taskScoreParameter);
				setTimeLimitMillis(task.timeLimitMillis);
				setMemoryLimitMB(task.memoryLimitMB);
				setInputTemplate(task.inputTemplate);
				setOutputTemplate(task.outputTemplate);
			})
		}
	}, [])
	const validateFields = () => {
		const requiredFields = {
			code,
			titleKA,
			timeLimitMillis,
			memoryLimitMB,
			inputTemplate,
			outputTemplate,
		};

		const validations = {};
		let isValid = true;

		for (const [fieldName, field] of Object.entries(requiredFields)) {
			validations[fieldName] = !!field;
			isValid = isValid && validations[fieldName];
		}

		setFieldValidations(validations);
		return isValid;
	};

	const handleNewTask = () => {
		if (!validateFields()) {
			toast.warn(getMessage('ka', 'missingRequiredFields'));
			return;
		}

		const params = {
			contestId: pageParams.contest_id,
			code: code.toString(),
			title: {
				KA: titleKA.toString(),
				EN: titleEN.toString(),
			},
			taskType: taskType.toString(),
			taskScoreType: taskScoreType.toString(),
			taskScoreParameter: parseFloat(taskScoreParameter),
			timeLimitMillis: parseInt(timeLimitMillis),
			memoryLimitMB: parseInt(memoryLimitMB),
			inputTemplate: inputTemplate.toString(),
			outputTemplate: outputTemplate.toString(),
		};

		axiosInstance
			.post('/task', params)
			.then((res) => {
				toast.success("ამოცანა წარმატებით დაემატა")
				setTaskId(res.data.taskDTO.id)
				var bodyFormData = new FormData()
				bodyFormData.append('taskId', res.data.taskDTO.id)
				bodyFormData.append('language', 'KA')
				bodyFormData.append('statement', kaStatement)
				console.log('BodyFromData', bodyFormData)
				axiosInstance({
					method: 'post',
					url: '/upload-statement',
					data: bodyFormData,
					headers: { 'Content-Type': 'multipart/form-data; boundary=<calculated when request is sent></calculated>' },
				})
					.then(function(response) {
						//handle success
						console.log(testCases)
						for (const testCase of testCases) {
							var bodyFormData = new FormData()
							bodyFormData.append('taskId', res.data.taskDTO.id)
							bodyFormData.append('file', testCase['testCaseFile'])
							console.log('BodyFromData', bodyFormData)
							console.log('TestCases', testCases)
							console.log('TestCase', testCase)
							axiosInstance({
								method: 'post',
								url: '/add-testcases',
								data: bodyFormData,
								headers: { 'Content-Type': 'multipart/form-data; boundary=<calculated when request is sent></calculated>' },
							})
								.then(function(response) {
									console.log(response)
								})
								.catch(function(response) {
									console.log(response)
								})
							console.log(response)
						}
					})
					.catch(function(response) {
						//handle error
						console.log(response)
					})
			})
	}
	return (
		<Container maxWidth='xs'>
			<Paper elevation={4} sx={{ padding: '1rem' }}>
				<Typography align='center' variant='h6' mb='1rem'>
					ახალი ამოცანა
				</Typography>
				<Stack gap='1rem' maxWidth='25rem' mx='auto' mb='1rem'>
					{/*	<Stack flexDirection='row' gap='1rem'>*/}
					<Button fullWidth variant='contained' component='label'>
						EN პირობა
						<input type='file' hidden onChange={(e) => setEnStatement(e.target.files[0])} />
					</Button>
					<Button fullWidth variant='contained' component='label'>
						KA პირობა
						<input type='file' hidden onChange={(e) => setKaStatement(e.target.files[0])} />
					</Button>
					{/*</Stack>*/}
					{/*<Stack flexDirection='row' gap='1rem'>*/}
					<TextField
						label={getMessage('ka', 'title') + ' (ka)'}
						value={titleKA}
						onChange={(e) => {
							setTitleKA(e.target.value);
							setFieldValidations({ ...fieldValidations, titleKA: true });
						}}
						variant='outlined'
						size='small'
						required
						error={!fieldValidations.titleKA}
					/>
					<TextField
						label={getMessage('ka', 'title') + ' (en)'}
						value={titleEN}
						onChange={(e) => setTitleEN(e.target.value)}
						variant='outlined'
						size='small'
					/>
					<TextField
						size='small'
						label={getMessage('ka', 'taskCode')}
						value={code}
						onChange={(e) => {
							setCode(e.target.value);
							setFieldValidations({ ...fieldValidations, code: true });
						}}
						required
						variant='outlined'
						error={!fieldValidations.code}
					/>
					<TextField
						select
						size='small'
						label={getMessage('ka', 'taskType')}
						value={taskType}
						onChange={(e) => setTaskType(e.target.value)}
						variant='outlined'
					>
						{taskTypes.map((option) => (
							<MenuItem key={option} value={option}>
								{getMessage('ka', 'TASK_TYPE_' + option)}
							</MenuItem>
						))}
					</TextField>
					<TextField
						select
						label={getMessage('ka', 'taskScoreType')}
						value={taskScoreType}
						onChange={(e) => setTaskScoreType(e.target.value)}
						variant='outlined'
						size='small'
						sx={{ minWidth: 'max-content' }}
					>
						{taskScoreTypes.map((option) => (
							<MenuItem key={option} value={option}>
								{getMessage('ka', 'TASK_SCORE_TYPE_' + option)}
							</MenuItem>
						))}
					</TextField>
					<TextField
						label={getMessage('ka', 'taskScoreParameter')}
						value={taskScoreParameter}
						onChange={(e) => setTaskScoreParameter(e.target.value)}
						variant='outlined'
						size='small'
					/>
					<TextField
						label={getMessage('ka', 'timeLimitMillis')}
						value={timeLimitMillis}
						onChange={(e) => {
							setTimeLimitMillis(e.target.value);
							setFieldValidations({ ...fieldValidations, timeLimitMillis: true });
						}}
						variant='outlined'
						size='small'
						required
						error={!fieldValidations.timeLimitMillis}
					/>
					<TextField
						label={getMessage('ka', 'memoryLimitMB')}
						value={memoryLimitMB}
						onChange={(e) => {
							setMemoryLimitMB(e.target.value);
							setFieldValidations({ ...fieldValidations, memoryLimitMB: true });
						}}
						variant='outlined'
						size='small'
						required
						error={!fieldValidations.memoryLimitMB}
					/>
					<TextField
						label={getMessage('ka', 'inputTemplate')}
						value={inputTemplate}
						onChange={(e) => {
							setInputTemplate(e.target.value);
							setFieldValidations({ ...fieldValidations, inputTemplate: true });
						}}
						variant='outlined'
						size='small'
						required
						error={!fieldValidations.inputTemplate}
					/>
					<TextField
						label={getMessage('ka', 'outputTemplate')}
						value={outputTemplate}
						onChange={(e) => {
							setOutputTemplate(e.target.value);
							setFieldValidations({ ...fieldValidations, outputTemplate: true });
						}}
						variant='outlined'
						size='small'
						required
						error={!fieldValidations.outputTemplate}
					/>
				</Stack>
				{testCases?.map((testCase, index) => (
					<Paper
						elevation={4}
						sx={{ padding: '1rem', marginBottom: '0.5rem' }}
						key={`${testCase?.testCaseFile?.name}@${testCase.selectedLanguage}`}
					>
						<Typography>
							<span style={{ fontWeight: 700 }}>#{index + 1} test: </span>
							{testCase?.testCaseFile?.name}
						</Typography>
					</Paper>
				))}
				{showNewTestCaseCard ? (
					<NewTestCaseCard taskId={taskId} handleTestCaseSubmit={handleTestCaseSubmit} />
				) : (
					<Paper elevation={4} sx={{ padding: '1rem', marginBottom: '0.5rem' }}>
						<Button
							fullWidth
							variant='contained'
							sx={{ background: '#3c324e' }}
							onClick={() => setShowNewTestCaseCard(true)}
						>
							ახალი ტესტ-ქეისები
						</Button>
					</Paper>
				)}
				<Button
					fullWidth
					variant='contained'
					size='large'
					sx={{ marginTop: '1rem', background: '#3c324e' }}
					onClick={() => {
						handleNewTask()
					}}
				>
					ამოცანის დამატება
				</Button>
			</Paper>
		</Container>
	)
}
