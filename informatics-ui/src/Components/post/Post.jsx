import React, {useContext, useEffect, useState} from 'react';
import {AxiosContext} from '../../utils/axiosInstance';
import ReactMarkdown from 'react-markdown';
import {Button, Box, Typography} from '@mui/material';
import {CommentsSection} from "./CommentSection";
import {AuthContext} from "../../store/authentication";
import remarkMath from "remark-math";
import rehypeMathjax from "rehype-mathjax";
import Card from "@mui/material/Card";
import getMessage from "../lang";
import {NavLink} from "react-router-dom";

export default function Post({id}) {
    const axiosInstance = useContext(AxiosContext);
    const authContext = useContext(AuthContext);
    const [post, setPost] = useState(null);

    const imageDownloadFunc = (url) => {

    }

    useEffect(() => {
        axiosInstance.get(`/post/${id}`)
            .then((response) => {
                setPost(response.data);
            })
            .catch((error) => {
                console.error('Error fetching post:', error);
            });
    }, [id, axiosInstance]);

    if (!post) {
        return <Typography>Loading...</Typography>;
    }
    return (
        <Card sx={{padding: '20px'}} key={id}>
            <Box sx={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                <Typography variant="h4">{post.title}</Typography>
                {post.authorName === authContext.username && (
                    <Button
                        variant="contained"
                        color="primary"
                        component={NavLink}
                        to={`/room/1/post/${id}`}
                    >
                        {getMessage('ka', 'edit')}
                    </Button>
                )}
            </Box>
            <Typography variant="subtitle1" color="textSecondary">
                {post.authorName}
            </Typography>
            <Box sx={{marginTop: '20px'}}>
                <ReactMarkdown
                    children={post.content}
                    remarkPlugins={[remarkMath]}
                    rehypePlugins={[rehypeMathjax]}
                    urlTransform={imageDownloadFunc}
                />
            </Box>
            {post.commentCount > 0 && (
                <Box sx={{marginTop: '40px'}}>
                    <Typography variant="h6" gutterBottom>Comments</Typography>
                    <CommentsSection postId={post.id} totalCount={post.commentCount}/>
                </Box>
            )}
        </Card>
    );
}