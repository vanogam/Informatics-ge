import {useContext, useEffect, useState} from 'react'
import Editor from 'react-simple-code-editor'
import {highlight, languages} from 'prismjs/components/prism-core'
import 'prismjs/components/prism-clike'
import 'prismjs/components/prism-c'
import 'prismjs/components/prism-cpp'
import 'prismjs/components/prism-python'
import {Box, Container, Typography, Paper, Button, CircularProgress, Stack} from '@mui/material'
import {useNavigate, useParams} from 'react-router-dom'
import AdminNavigationBar from '../../Components/AdminNavigationBar'
import {useRequireAdmin} from '../../utils/useRequireAdmin'
import {AxiosContext} from '../../utils/axiosInstance'
import {formatDateTime} from '../../utils/dateUtils'
import getMessage from '../../Components/lang'

const PRISM_LANGUAGE_BY_CODE = {
	CPP: ['cpp', languages.cpp],
	PYTHON: ['python', languages.python],
}

function CodePanel({title, submissionTime, code, prismLanguage}) {
	const [grammarName, grammar] = PRISM_LANGUAGE_BY_CODE[prismLanguage] || PRISM_LANGUAGE_BY_CODE.CPP
	return (
		<Paper elevation={4} sx={{padding: '1rem', flex: 1, minWidth: 0}}>
			<Typography sx={{fontSize: '13px', fontWeight: 'bold'}}>{title}</Typography>
			<Typography sx={{fontSize: '11px', color: 'text.secondary', marginBottom: '0.5rem'}}>
				{formatDateTime(submissionTime)}
			</Typography>
			<Box sx={{overflowX: 'auto', userSelect: 'contain', WebkitUserSelect: 'contain'}}>
				<Editor
					value={code || ''}
					onValueChange={() => {}}
					disabled
					highlight={(value) => highlight(value, grammar, grammarName)}
					style={{
						overflowY: 'auto',
						maxHeight: '32rem',
						fontFamily: '"Fira code", "Fira Mono", monospace',
						fontSize: 12,
					}}
				/>
			</Box>
		</Paper>
	)
}

export default function PlagiarismCompare() {
	const {ready} = useRequireAdmin()
	const {comparisonId} = useParams()
	const axiosInstance = useContext(AxiosContext)
	const navigate = useNavigate()

	const [detail, setDetail] = useState(null)
	const [loading, setLoading] = useState(true)
	const [error, setError] = useState(null)

	useEffect(() => {
		if (!ready) {
			return
		}
		axiosInstance
			.get(`/admin/plagiarism/comparisons/${comparisonId}`)
			.then((response) => setDetail(response.data))
			.catch(() => setError(getMessage('ka', 'plagiarismComparisonNotFound')))
			.finally(() => setLoading(false))
	}, [ready, axiosInstance, comparisonId])

	if (!ready || loading) {
		return (
			<>
				<AdminNavigationBar />
				<Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
					<CircularProgress />
				</Box>
			</>
		)
	}

	if (error || !detail) {
		return (
			<>
				<AdminNavigationBar />
				<Container maxWidth="lg">
					<Typography color="error" align="center" mt="2rem">
						{error || getMessage('ka', 'plagiarismComparisonNotFound')}
					</Typography>
				</Container>
			</>
		)
	}

	return (
		<main>
			<AdminNavigationBar />
			<Container maxWidth={false} sx={{maxWidth: '1400px'}}>
				<Button sx={{marginTop: '1rem'}} onClick={() => navigate(-1)}>
					&larr; {getMessage('ka', 'plagiarismBackToJobs')}
				</Button>

				<Paper elevation={3} sx={{padding: '1.5rem', marginTop: '1rem', marginBottom: '1.5rem'}}>
					<Typography variant="h6" sx={{color: '#452c54', fontWeight: 'bold'}}>
						{getMessage('ka', 'plagiarismCompareTitle')}: {detail.taskTitle}
					</Typography>
					<Typography variant="body1" sx={{marginTop: '0.5rem'}}>
						{getMessage('ka', 'plagiarismSimilarityColumn')}: <b>{(detail.similarity * 100).toFixed(1)}%</b>
					</Typography>
				</Paper>

				<Stack direction={{xs: 'column', md: 'row'}} spacing={2}>
					<CodePanel title={detail.usernameA} submissionTime={detail.submissionTimeA} code={detail.codeA} prismLanguage={detail.language} />
					<CodePanel title={detail.usernameB} submissionTime={detail.submissionTimeB} code={detail.codeB} prismLanguage={detail.language} />
				</Stack>
			</Container>
		</main>
	)
}
