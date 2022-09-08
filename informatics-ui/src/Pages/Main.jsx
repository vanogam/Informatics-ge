import * as React from 'react'
import Card from '@mui/material/Card'
import CardActions from '@mui/material/CardActions'
import CardContent from '@mui/material/CardContent'
import CardMedia from '@mui/material/CardMedia'
import Button from '@mui/material/Button'
import Typography from '@mui/material/Typography'
import news1 from '../assets/news1.jpg'
import news2 from '../assets/news2.jpg'
import news3 from '../assets/news3.jpg'
import { Box } from '@mui/material'
import axios from 'axios'
import { useState, useEffect, useContext } from 'react'
import { AuthContext } from '../store/authentication'
import { NavLink} from 'react-router-dom'
import NewNews from './NewNews'
function loadNews(response, setNews){

}

export default function Main() {
	const authContext = useContext(AuthContext)
	const [news, setNews] = useState([])
	const [roles, setRoles] = useState()	
	useEffect(() => {
		axios
			.get(`${process.env.REACT_APP_HOST}/contest-list`, {
				params: {
					roomId: 1,
				},
			})
			.then((response) =>loadNews(response, setNews))
			.catch((error) => console.log(error))
		setRoles(() => localStorage.getItem('roles'))
	}, [])

	const dummy_news_list = [
		{
			title : 'დათო ჩეჩელაშვილი Amazon-ში იწყებს მუშაობას',
			text : "თავისუფალი უნივერსიტეტის MACS[E]-ის სკოლის მათემატიკისა და კომპიუტერული მეცნიერების მიმართულების მეოთხე კურსის სტუდენტი, დათო	ჩეჩელაშვილი ამაზონის ბერლინის ოფისს Software Development Engineer-ის პოზიციაზე შეუერთდება...",
			image: news1
		},
		{
			title : 'საბა ცერცვაძე Google-ის გუნდს შეუერთდება',
			text : "	თავისუფალი უნივერსიტეტის MACS[E]-ის სკოლის კომპიუტერული	მეცნიერებების პროგრამის სტუდენტი, საბა ცერცვაძე სექტემბრიდან	Google-ის ვარშავის ოფისს Google Cloud Engine Managed Infrastructure-ის (GCE Minfra) გუნდში შეუერთდება Software Engineer-ის პოზიციაზე...",
			image: news2
		},
		{
			title :'სტეფანე გურგენიძე მუშაობას Microsoft-დან Amazon-ში აგრძელებს',
			text : "თავისუფალი უნივერსიტეტის MACS[E]-ის სკოლის კომპიუტერული მეცნიერებების პროგრამის სტუდენტი, სტეფანე გურგენიძე მუშაობას Amazon-ის ბარსელონას ოფისში იწყებს Software Development Engineer-ის პოზიციაზე...",
			image: news3
		},	

	]

	
	return (
		<Box>
			<Box>
					<Box sx={{ marginLeft: '15%', marginTop: '5%' }}>
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
					width: '70%',
				}}
			/>
				{roles === 'ADMIN' && (
					<Button
						variant="contained"
						color="secondary"
						sx={{ marginLeft:'74%', backgroundColor: '#2f2d47' }}
						component={NavLink}
						to="/newNews"
					>
						დაამატე სიახლე
					</Button>
				)}
				
				{dummy_news_list.map((news) => (
					<Card	sx={{
						marginTop: '3%',
						marginBottom: '0',
						marginLeft: '15%',
						marginRight: '10%',
						maxWidth: '40%',
						maxHeight: '10%',
					}}>
				<CardMedia width="20" component="img" height="200" image={news.image} />
				<CardContent>
					<Typography gutterBottom variant="h5" component="div">
					{news.title}
					</Typography>
					<Typography variant="body2" color="text.secondary">
					{news.text}
					</Typography>
				</CardContent>
				<CardActions>
					<Button size="small">ვრცლად</Button>
				</CardActions>
			</Card>
				))}

		</Box>
	)
}
