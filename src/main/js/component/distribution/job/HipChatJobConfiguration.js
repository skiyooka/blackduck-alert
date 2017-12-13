'use strict';
import React from 'react';

import TextInput from '../../../field/input/TextInput';
import CheckboxInput from '../../../field/input/CheckboxInput';

import BaseJobConfiguration from './BaseJobConfiguration';

export default class HipChatJobConfiguration extends BaseJobConfiguration {
	constructor(props) {
		super(props);
	}

	render() {
		let content = <div>
							<TextInput label="Room Name" name="roomName" value={this.props.roomName} onChange={this.handleChange} errorName="roomNameError" errorValue={this.props.roomNameError}></TextInput>
							<CheckboxInput label="Notify" name="notify" value={this.props.notify} onChange={this.handleChange} errorName="notifyError" errorValue={this.props.notifyError}></CheckboxInput>
							<TextInput label="Color" name="color" value={this.props.color} onChange={this.handleChange} errorName="colorError" errorValue={this.props.colorError}></TextInput>
						</div>;

		return super.render(content);
	}
}

HipChatJobConfiguration.propTypes = {
    baseUrl: PropTypes.string,
    testUrl: PropTypes.string,
    distributionType: PropTypes.string
};

HipChatJobConfiguration.defaultProps = {
    baseUrl: '/configuration/distribution/hipchat',
    testUrl: '/configuration/distribution/hipchat/test',
    distributionType: 'HipChat'
};