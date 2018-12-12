import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import DescriptorLabel from '../../common/DescriptorLabel';
import TextInput from '../../../field/input/TextInput';
import TextArea from '../../../field/input/TextArea';
import {BootstrapTable, ReactBsTable, TableHeaderColumn} from 'react-bootstrap-table';
import RefreshTableCellFormatter from "../../common/RefreshTableCellFormatter";
import {Modal, Tab, Tabs} from "react-bootstrap";

class Details extends Component {
    constructor(props) {
        super(props);

        this.state = {
            message: ''
        };

        this.resendButton = this.resendButton.bind(this);
        this.onResendClick = this.onResendClick.bind(this);
        this.getEventType = this.getEventType.bind(this);
    }

    getEventType(eventType) {
        const defaultValue = <div className="inline">Unknown</div>;
        if (this.props.descriptors) {
            const descriptorList = this.props.descriptors.items['CHANNEL_DISTRIBUTION_CONFIG'];
            if (descriptorList) {
                const filteredList = descriptorList.filter(descriptor => descriptor.descriptorName === eventType)
                if (filteredList && filteredList.length > 0) {
                    const foundDescriptor = filteredList[0];
                    return (<DescriptorLabel keyPrefix='audit-detail-icon' descriptor={foundDescriptor}/>);
                }
            }
        }
        return (defaultValue);
    }

    expandComponent(row) {
        let errorMessage = null;
        if (row.errorMessage) {
            errorMessage = <TextInput label="Error" readOnly name="errorMessage" value={row.errorMessage}/>;
        }
        let errorStackTrace = null;
        if (row.errorStackTrace) {
            errorStackTrace = <TextArea inputClass="textArea" label="Stack Trace" readOnly name="errorStackTrace" value={row.errorStackTrace}/>;
        }

        return (<div className="inline">{errorMessage}{errorStackTrace}</div>);
    }

    onResendClick(currentRowSelected) {
        const currentEntry = currentRowSelected || this.state.currentRowSelected;
        this.props.resendNotification(this.props.currentEntry.id, currentEntry.configId)
    }

    resendButton(cell, row) {
        return (<RefreshTableCellFormatter handleButtonClicked={this.onResendClick} currentRowSelected={row} buttonText="Re-send"/>);
    }

    render() {
        const jobTableOptions = {
            defaultSortName: 'timeLastSent',
            defaultSortOrder: 'desc',
            btnGroup: this.createCustomButtonGroup,
            noDataText: 'No events',
            clearSearch: true,
            expandBy: 'column',
            expandRowBgColor: '#e8e8e8',
        };
        let jsonContent = null;
        if (this.props.currentEntry.content) {
            jsonContent = JSON.parse(this.props.currentEntry.content);
        } else {
            jsonContent = Object.assign({}, {'warning': 'Content in an Unknown Format'});
        }
        const jsonPrettyPrintContent = JSON.stringify(jsonContent, null, 2);
        const jobs = this.props.currentEntry.jobs;
        return (
            <Modal size="lg" show={this.props.show} onHide={this.props.handleClose}>
                <Modal.Header closeButton>
                    <Modal.Title>
                        <div className="notificationTitlePiece">
                            {this.props.providerNameFormat(this.props.currentEntry.provider)}
                        </div>
                        <div className="notificationTitlePiece">
                            {this.props.notificationTypeFormat(this.props.currentEntry.notificationType)}
                        </div>
                        <div className="notificationTitlePiece">
                            {this.props.currentEntry.createdAt}
                        </div>
                        <div className="notificationTitlePiece">
                            {this.props.statusFormat(this.props.currentEntry.overallStatus)}
                        </div>
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <div className="expandableContainer">
                        <Tabs defaultActiveKey={1} id="uncontrolled-tab-example">
                            <Tab eventKey={1} title="Distribution Jobs">
                                <div className="container-fluid">
                                    <BootstrapTable
                                        version="4"
                                        trClassName={this.trClassFormat}
                                        condensed
                                        data={jobs}
                                        expandableRow={() => true}
                                        expandComponent={this.expandComponent}
                                        containerClass="table"
                                        options={jobTableOptions}
                                        headerContainerClass="scrollable"
                                        bodyContainerClass="auditTableScrollableBody"
                                        pagination
                                        search
                                    >
                                        <TableHeaderColumn dataField="name" dataSort columnTitle columnClassName="tableCell">Distribution Job</TableHeaderColumn>
                                        <TableHeaderColumn dataField="eventType" dataSort columnClassName="tableCell" dataFormat={this.getEventType}>Event Type</TableHeaderColumn>
                                        <TableHeaderColumn dataField="timeLastSent" dataSort columnTitle columnClassName="tableCell">Time Last Sent</TableHeaderColumn>
                                        <TableHeaderColumn dataField="status" dataSort columnClassName="tableCell" dataFormat={this.props.statusFormat}>Status</TableHeaderColumn>
                                        <TableHeaderColumn dataField="" width="48" expandable={false} columnClassName="tableCell" dataFormat={this.resendButton}/>
                                        <TableHeaderColumn dataField="configId" hidden>Job Id</TableHeaderColumn>
                                        <TableHeaderColumn dataField="id" isKey hidden>Audit Id</TableHeaderColumn>
                                    </BootstrapTable>
                                </div>
                            </Tab>
                            <Tab eventKey={2} title="Notification Content">
                                <div className="tableContainer">
                                    <TextArea inputClass="auditContentTextArea" sizeClass='col-sm-12' label="" readOnly name="notificationContent" value={jsonPrettyPrintContent}/>
                                </div>
                            </Tab>
                        </Tabs>
                    </div>
                </Modal.Body>

            </Modal>
        );
    }
}

Details.propTypes = {
    descriptors: PropTypes.object
};

Details.defaultProps = {
    descriptors: {}
};

const mapStateToProps = state => ({
    descriptors: state.descriptors
});

const mapDispatchToProps = dispatch => ({});

export default connect(mapStateToProps, mapDispatchToProps)(Details);
