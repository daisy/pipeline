package main

import "path/filepath"

//Differences between two release descriptors
type Diff struct {
	New *Artifact
	Old *Artifact
}

//Gets the artifact to be downloaded from this difference
func (d Diff) ToDownload() (a Artifact, ok bool) {
	if d.New != nil {
		a = *d.New
		ok = true
	}
	return

}

//Gets the artifact that needs to be uninstalled from this diff
func (d Diff) ToRemove() (a Artifact, ok bool) {
	if d.Old != nil {
		a = *d.Old
		ok = true
	}
	return
}

//set of differences
type DiffSet []Diff

//set of artifacts that need to be downloaded from the difference set
func (ds DiffSet) ToDownload() []Artifact {
	as := []Artifact{}
	for _, d := range ds {
		if a, ok := d.ToDownload(); ok {
			as = append(as, a)
		}
	}
	return as

}

//set of artifacts that need to deleted from the local installation
func (ds DiffSet) ToRemove(installPath string) []LocalArtifact {
	las := []LocalArtifact{}
	for _, d := range ds {
		if a, ok := d.ToRemove(); ok {
			la := LocalArtifact{
				Artifact: a,
				Path:     filepath.Join(installPath, a.DeployPath),
			}
			las = append(las, la)
		}
	}
	return las
}
