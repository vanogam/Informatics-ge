import * as React from 'react'
import Card from '@mui/material/Card'
import CardActions from '@mui/material/CardActions'
import CardContent from '@mui/material/CardContent'
import CardMedia from '@mui/material/CardMedia'
import Button from '@mui/material/Button'
import Typography from '@mui/material/Typography'
import { Box } from '@mui/material'
import { useState, useEffect, useContext } from 'react'
import { AuthContext } from '../store/authentication'
import { NavLink } from 'react-router-dom'
import { AxiosContext } from '../utils/axiosInstance'
import getMessage from '../Components/lang'
import Post from "../Components/post/Post";

export default function Main() {
	const axiosInstance = useContext(AxiosContext)
	const authContext = useContext(AuthContext)
	const [news, setNews] = useState([])
	useEffect(() => {
		loadNews();
	}, [])

	const loadNews = () => {
		axiosInstance.get('/room/1/posts')
			.then((response) => {
				if (response.status === 200) {
					setNews(response.data.posts)
				} else {
					console.error('Failed to load news:', response.statusText)
				}
			})
	}

	return (
		<Box>
			<Box>
				<Box sx={{ marginLeft: '5%', marginTop: '5%' }}>
					<Button
						sx={{
							marginInline: '2px',
							alignSelf: 'flex-end',
							color: '#4a366c',
							fontWeight: 'bold',
							fontFamily: '"Lucida Console", "Courier New", monospace',
							fontSize: '1.5rem',
						}}
					>
						სიახლეები
					</Button>
				</Box>

			</Box>

			<hr
				style={{
					color: '#a48fca',
					backgroundColor: '#2c1c48',
					height: 5,
					width: '90%',
				}}
			/>
			{authContext.role === 'ADMIN' && (
				<Button
					variant='contained'
					color='secondary'
					sx={{ marginLeft: '85%', backgroundColor: '#2f2d47' }}
					component={NavLink}
					to='/room/1/post'
				>
					{getMessage('ka', 'addPost')}
				</Button>
			)}
			<Box style={{
				marginTop: 20,
				width: '90%',
				marginLeft: '5%',
			}}>
				{Array.isArray(news) && news.map((newsItem) => (
					<Post
						id={newsItem.id}
						key={newsItem.id}
					/>
				))}
			</Box>
		</Box>
	)
}
