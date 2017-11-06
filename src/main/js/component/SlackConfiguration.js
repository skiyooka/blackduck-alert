import React from 'react';
import Field from '../field/Field';

class SlackConfiguration extends React.Component {
	//constructor is part of the Component lifecycle
	constructor(props) {
		super(props);
		this.state = {
				id: undefined,
				channelName: '',
				username: '',
				webhook: '',

				configurationMessage: '',
				channelNameError: '',
				usernameError: '',
				webhookError: ''
		};
		this.handleChange = this.handleChange.bind(this);
	}

	resetMessageStates() {
		this.setState({
			configurationMessage: '',
			channelNameError: '',
			usernameError: '',
			webhookError: ''
		});
	}

	//componentDidMount is part of the Component lifecycle, executes after construction
	componentDidMount() {
		this.resetMessageStates();
		var self = this;
		fetch('/configuration/slack')  
		.then(function(response) {
			if (!response.ok) {
				return response.json().then(json => {
					self.setState({
						configurationMessage: json.message
					});
				});
			} else {
				return response.json();
			}
		}).then(function(body) {
			if (body != null && body.length > 0) {
				var configuration = body[0];
				self.setState({
					id: configuration.id,
					apiKchannelNameey: configuration.channelName,
					username: configuration.username,
					webhook: configuration.webhook
				});
			}
		});
	}

	handleSubmit(event) {
		this.resetMessageStates();
		event.preventDefault();
		var self = this;
		let jsonBody = JSON.stringify(this.state);
		var method = 'POST';
		if (this.state.id) {
			method = 'PUT';
		}
		fetch('/configuration/slack', {
			method: method,
			headers: {
				'Content-Type': 'application/json'
			},
			body: jsonBody
		}).then(function(response) {
			return response.json().then(json => {
				let errors = json.errors;
				if (errors) {
					for (var key in errors) {
						if (errors.hasOwnProperty(key)) {
							let name = key.concat('Error');
							let value = errors[key];
							self.setState({
								[name]: value
							});
						}
					}
				}
				self.setState({
					configurationMessage: json.message
				});
			});
		});
	}

	handleTestSubmit(event) {
		this.resetMessageStates();
		event.preventDefault();
		var self = this;
		let jsonBody = JSON.stringify(this.state);
		fetch('/configuration/slack/test', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: jsonBody
		}).then(function(response) {
			return response.json().then(json => {
				let errors = json.errors;
				if (errors) {
					for (var key in errors) {
						if (errors.hasOwnProperty(key)) {
							let name = key.concat('Error');
							let value = errors[key];
							self.setState({
								[name]: value
							});
						}
					}
				}
				self.setState({
					configurationMessage: json.message
				});
			});
		});
	}

	handleChange(event) {
		const target = event.target;
		const value = target.type === 'checkbox' ? target.checked : target.value;
		const name = target.name;

		this.setState({
			[name]: value
		});
	}
	
	render() {
		return (
				<div>
				<h1>Slack Configuration</h1>
				<form onSubmit={this.handleSubmit.bind(this)}>
				<Field label="Channel Name" type="text" name="channelName" value={this.state.channelName} onChange={this.handleChange} errorName="channelNameError" errorValue={this.state.channelNameError}></Field>
				
				<Field label="Username" type="text" name="username" value={this.state.username} onChange={this.handleChange} errorName="usernameError" errorValue={this.state.usernameError}></Field>
				
				<Field label="Webhook" type="text" name="webhook" value={this.state.webhook} onChange={this.handleChange} errorName="webhookError" errorValue={this.state.webhookError}></Field>
				
				<input type="submit" value="Save"></input>
				<input type="button" value="Test" onClick={this.handleTestSubmit.bind(this)}></input>
				<p name="configurationMessage">{this.state.configurationMessage}</p>
				</form>
				</div>
		)
	}
}

export default SlackConfiguration;
