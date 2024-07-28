import { Button, Container, MenuItem, Paper, Stack, TextField, Typography } from '@mui/material'
import { useContext, useRef, useState } from 'react'
import NewTestCaseCard from './NewTestCaseCard'
import { AxiosContext, getAxiosInstance } from '../utils/axiosInstance'
import getMessage from './lang'
import { toast } from 'react-toastify'
import { useParams } from 'react-router-dom'

export default function NewTaskCard() {
	const pageParams = useParams();
	const codeRef = useRef(null)
	const titleENref = useRef(null)
	const titleKAref = useRef(null)
	const taskTypeRef = useRef('BATCH')
	const taskScoreTypeRef = useRef('SUM')
	const taskScoreParameterRef = useRef(null)
	const timeLimitMillisRef = useRef(null)
	const memoryLimitMBref = useRef(null)
	const inputTemplateRef = useRef(null)
	const outputTemplateRef = useRef(null)
	const enStatementRef = useRef(null)
	const kaStatementRef = useRef(null)
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
	const validateFields = () => {
		const requiredFields = {
			code: codeRef.current,
			titleKA: titleKAref.current,
			timeLimitMillis: timeLimitMillisRef.current,
			memoryLimitMB: memoryLimitMBref.current,
			inputTemplate: inputTemplateRef.current,
			outputTemplate: outputTemplateRef.current,
		};

		const validations = {};
		let isValid = true;

		for (const [fieldName, field] of Object.entries(requiredFields)) {
			validations[fieldName] = !!field && ((field.value && field.value.trim()) || (field.files && field.files.length));
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
		const code = codeRef?.current.value.toString()
		const enStatement = enStatementRef?.current.files[0]
		const kaStatement = kaStatementRef?.current.files[0]
		const enTitle = titleENref?.current.value
		const kaTitle = titleKAref?.current.value
		const taskType = taskTypeRef?.current.value
		const taskScoreType = taskScoreTypeRef?.current.value
		const taskScoreParameter = taskScoreParameterRef?.current.value.toString()
		const timeLimitMillis = timeLimitMillisRef?.current.value
		const memoryLimitMB = memoryLimitMBref?.current.value
		const inputTemplate = inputTemplateRef?.current.value
		const outputTemplate = outputTemplateRef?.current.value
		const params = {
			'contestId': pageParams.contest_id,
			'code': code.toString(),
			'title': {
				'KA': kaTitle.toString(),
				'EN': enTitle.toString(),
			},
			'taskType': taskType.toString(),
			'taskScoreType': taskScoreType.toString(),
			'taskScoreParameter': parseFloat(taskScoreParameter),
			'timeLimitMillis': parseInt(timeLimitMillis),
			'memoryLimitMB': parseInt(memoryLimitMB),
			'inputTemplate': inputTemplate.toString(),
			'outputTemplate': outputTemplate.toString(),
		}
		console.log({
			code,
			enStatement,
			kaStatement,
			enTitle,
			kaTitle,
			taskType,
			taskScoreType,
			taskScoreParameter,
			timeLimitMillis,
			memoryLimitMB,
			inputTemplate,
			outputTemplate,
		})
		axiosInstance
			.post('/save-task', params)
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
									//handle success
									console.log(response)
								})
								.catch(function(response) {
									//handle error
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
						<input ref={enStatementRef} type='file' hidden />
					</Button>
					<Button fullWidth variant='contained' component='label'>
						KA პირობა
						<input ref={kaStatementRef} type='file' hidden />
					</Button>
					{/*</Stack>*/}
					{/*<Stack flexDirection='row' gap='1rem'>*/}
					<TextField
						label={getMessage('ka', 'title') + ' (ka)'}
						inputRef={titleKAref}
						variant='outlined'
						size='small'
						required
						error={!fieldValidations.titleKA}
						onChange={() => setFieldValidations({
							...fieldValidations,
							titleKA: true
						})}
					/>
					<TextField
						label={getMessage('ka', 'title') + ' (en)'}
						inputRef={titleENref}
						variant='outlined'
						size='small'
					/>
					<TextField size='small'
										 label={getMessage('ka', 'taskCode')}
										 inputRef={codeRef}
										 required
										 variant='outlined'
										 error={!fieldValidations.code}
										 onChange={() => setFieldValidations({
											 ...fieldValidations,
											 code: true
										 })}
					/>
					<TextField
						select
						size='small'
						label={getMessage('ka', 'taskType')}
						defaultValue={'BATCH'}
						inputRef={taskTypeRef} variant='outlined'>
						{taskTypes.map((option) => (
							<MenuItem key={option} value={option}>
								{getMessage('ka', 'TASK_TYPE_' + option)}
							</MenuItem>
						))}
					</TextField>
					<TextField
						select
						label={getMessage('ka', 'taskScoreType')}
						inputRef={taskScoreTypeRef}
						defaultValue={'SUM'}
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
						inputRef={taskScoreParameterRef}
						variant='outlined'
						size='small'
					/>
					{/*</Stack>*/}
					{/*<Stack flexDirection='row' gap='1rem'>*/}

					<TextField
						label={getMessage('ka', 'timeLimitMillis')}
						inputRef={timeLimitMillisRef}
						variant='outlined'
						size='small'
						required
						error={!fieldValidations.timeLimitMillis}
						onChange={() => setFieldValidations({
							...fieldValidations,
							timeLimitMillis: true
						})}
					/>
					<TextField
						label={getMessage('ka', 'memoryLimitMB')}
						inputRef={memoryLimitMBref}
						variant='outlined'
						size='small'
						required
						error={!fieldValidations.memoryLimitMB}
						onChange={() => setFieldValidations({
							...fieldValidations,
							memoryLimitMB: true
						})}
					/>
					{/*</Stack>*/}
					{/*<Stack flexDirection='row' gap='1rem'>*/}
					<TextField
						label={getMessage('ka', 'inputTemplate')}
						inputRef={inputTemplateRef}
						variant='outlined'
						size='small'
						required
						error={!fieldValidations.inputTemplate}
						onChange={() => setFieldValidations({
							...fieldValidations,
							inputTemplate: true
						})}
					/>

					<TextField
						label={getMessage('ka', 'outputTemplate')}
						inputRef={outputTemplateRef}
						variant='outlined'
						size='small'
						required
						error={!fieldValidations.outputTemplate}
						onChange={() => setFieldValidations({
							...fieldValidations,
							outputTemplate: true
						})}
					/>
					{/*</Stack>*/}
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
						>ახალი ამოცანა
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
