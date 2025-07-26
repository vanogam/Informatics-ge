import React, { useState, useEffect, useContext, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { AxiosContext } from '../utils/axiosInstance';
import { Box, Typography } from '@mui/material';
import MarkdownEditor from '../Components/markdownEditor';
import getMessage from "../Components/lang";
import {youtubeEmbedRegex, youtubeRegex} from "../utils/constants";

function AddPost() {
    const { room_id, post_id } = useParams();
    const axiosInstance = useContext(AxiosContext);
    const navigate = useNavigate();
    const [postId, setPostId] = useState(null);
    const [content, setContent] = useState({});
    const [loading, setLoading] = useState(false);
    const [draftSaved, setDraftSaved] = useState(false);
    const [version, setVersion] = useState(0);
    const postIdRef = useRef(postId);
    const versionRef = useRef(version);
    const draftSavedRef = useRef(draftSaved);
    const contentRef = useRef(content);

    useEffect(() => {
        if (post_id) {
            setPostId(post_id);
            postIdRef.current = post_id;
            loadPost();
        } else {
            updateDraft(true);
        }

        const interval = setInterval(() => {
            if (!draftSavedRef.current) {
                updateDraft(false);
            }
        }, 10000);

        return () => clearInterval(interval);
    }, [room_id, post_id, axiosInstance]);

    useEffect(() => {
        draftSavedRef.current = draftSaved;
    }, [draftSaved]);

    useEffect(() => {
        setDraftSaved(false);
        contentRef.current = content;
    }, [content]);

    const loadPost = () => {
        axiosInstance.get(`/post/${post_id}`)
            .then((response) => {
                setVersion(response.data.version);
                versionRef.current = response.data.version;
                setContent({title: response.data.title,
                                  body: response.data.content});
                setLoading(false);
                setDraftSaved(true);
            })
            .catch((error) => {
                setLoading(false);
                console.error('Error creating draft post:', error);
            });
    }

    const updateDraft = (isInitial) => {
        const content = contentRef.current;
        if (loading) {
            return;
        }
        console.log(postIdRef.current + " " + isInitial)
        setLoading(true);
        axiosInstance.post(`/room/${room_id}/post`, {
            id: postIdRef.current,
            title: null,
            content: null,
            draftContent: JSON.stringify(content),
            roomId: room_id,
            status: 'DRAFT',
            version: versionRef.current,
        })
            .then((response) => {
                setPostId(response.data.post.id);
                postIdRef.current = response.data.post.id;
                setVersion(response.data.post.version);
                versionRef.current = response.data.post.version;
                if (isInitial) {
                    setContent(response.data.post.draftContent ? JSON.parse(response.data.post.draftContent) : { title: '', body: '' });
                }
                setLoading(false);
                setDraftSaved(true);
            })
            .catch((error) => {
                setLoading(false);
                console.error('Error creating draft post:', error);
            });
    }

    const handleSave = (content) => {
        setLoading(true);
        axiosInstance.put(`/post/${postId}`, { id: postId,
                                               title: content.title,
                                               content: content.body,
                                               draftContent: JSON.stringify(content),
                                               roomId: room_id,
                                               status: 'PUBLISHED',
                                               version: versionRef.current
        })
            .then(() => {
                setLoading(false);
                navigate(`/room/${room_id}/post/${postId}`);
            })
            .catch((error) => {
                setLoading(false);
                console.error('Error saving post:', error);
            });
    };

    return (
        <Box sx={{ padding: '20px' }}>
            <Typography variant="h4" sx={{ marginBottom: '20px' }}>{getMessage('ka', 'addPost')}</Typography>
            <MarkdownEditor
                value={content}
                onChange={setContent}
                entries={[
                    {
                        align: 'center',
                        labelVisible: false,
                        label: getMessage('ka', 'title'),
                        value: content.title,
                        onChange: (value) => {
                            if (typeof value === 'function') {
                                value = value(content.title);
                            }
                            setContent({...content, title: value})
                        },
                        height: "2rem",
                    },
                    {
                        labelVisible: false,
                        label: getMessage('ka', 'body'),
                        value: content.body,
                        onChange: (value) => {
                            if (typeof value === 'function') {
                                value = value(content.body);
                            }
                            console.log(value)
                            setContent({...content, body: value})
                        },
                        height: "20rem",
                    },
                ]}
                imageUploadAddress={`/post/${postId}/image`}
                imageDownloadFunc={url => {
                    console.log("???" + url, youtubeEmbedRegex.test(url), youtubeEmbedRegex)
                    if (youtubeEmbedRegex.test(url)) {
                        console.log("YES")
                        return url; // Directly return YouTube URLs
                    }
                    return `/api/post/${postId}/image/${url}`;
                }}
                saveText={loading ? getMessage('ka', 'loading') : getMessage('ka', 'publish')}
                loading={loading}
                comment={draftSaved ? getMessage('ka', 'draftSaved') : null}
                submitFunc={handleSave}
            />
        </Box>
    );
}

export default AddPost;