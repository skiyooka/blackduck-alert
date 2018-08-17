import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import ReadOnlyField from '../../field/ReadOnlyField';
import { getAboutInfo } from '../../store/actions/about';

class AboutInfo extends React.Component {
    constructor(props) {
        super(props);
    }

    componentDidMount() {
        this.props.getAboutInfo();
    }

    iconColumnRenderer(cell) {
        const altText = cell;
        const keyText = `aboutIconKey-${cell}`;
        const classNameText= `fa fa-${cell}`;
        return (<span key={keyText} alt={altText} className={classNameText} aria-hidden="true" />);
    }

    createDescriptorTable(tableData) {
        const tableOptions = {
            defaultSortName: 'name',
            defaultSortOrder: 'asc',
            noDataText: 'No data found',
        };
        return (
            <div className="form-group">
                <BootstrapTable
                    data={tableData}
                    options={tableOptions}
                    headerContainerClass="scrollable"
                    bodyContainerClass="scrollable">
                    <TableHeaderColumn dataField="iconKey" className="iconTableRow" columnClassName="iconTableRow" dataFormat={this.iconColumnRenderer}>
                    </TableHeaderColumn>
                    <TableHeaderColumn dataField="name" isKey>
                        Name
                    </TableHeaderColumn>
                </BootstrapTable>
            </div>
        );
    }

    render() {
        const { version,description, projectUrl, channelList, providerList } = this.props;
        const projectUrlLink = <a alt={projectUrl} href={projectUrl}>{projectUrl}</a>;
        const providerTable = this.createDescriptorTable(providerList);
        const channelTable = this.createDescriptorTable(channelList);
        return (
            <div>
                <h1>
                    <span className="fa fa-info"/>
                    About
                </h1>
                <div className="form-horizontal">
                    <ReadOnlyField label="Description" name="description" readOnly="true" value={description}/>
                    <ReadOnlyField label="Version" name="version" readOnly="true" value={version}/>
                    <ReadOnlyField label="Project URL" name="projectUrl" readOnly="true" value={projectUrlLink}/>
                    <div className="form-group">
                        <ReadOnlyField label="Supported Providers" name="providerTable" readOnly="true" value={providerTable}/>
                    </div>
                    <div className="form-group">
                        <ReadOnlyField label="Supported Distribution Channels" name="channelTable" readOnly="true" value={channelTable}/>
                    </div>
                </div>
            </div>
        );
    }
}

AboutInfo.propTypes = {
    fetching: PropTypes.bool,
    version: PropTypes.string.isRequired,
    description: PropTypes.string,
    projectUrl: PropTypes.string.isRequired,
    channelList: PropTypes.arrayOf(PropTypes.object),
    providerList: PropTypes.arrayOf(PropTypes.object)
};

AboutInfo.defaultProps = {
    fetching: false,
    version: '',
    description: '',
    projectUrl: '',
    channelList: [],
    providerList: []
};

const mapStateToProps = state => ({
    fetching: state.about.fetching,
    version: state.about.version,
    description: state.about.description,
    projectUrl: state.about.projectUrl,
    channelList: state.about.channelList,
    providerList: state.about.providerList
});

const mapDispatchToProps = dispatch => ({
    getAboutInfo: () => dispatch(getAboutInfo())
});

export default connect(mapStateToProps, mapDispatchToProps)(AboutInfo);