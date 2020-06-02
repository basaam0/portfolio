// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random personal fact to the page.
 */
function addRandomFact() {
  const facts = [
    'I have 2 siblings.',
    'My favorite color is blue.',
    'I enjoy outdoor activities.',
    'I skipped a grade.',
    'I was born in Massachusetts.',
  ];

  // Pick a random fact.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

/**
 * Cycles backwards to the previous background image.
 */
function prevBackground() {
  changeBackground(-1);
}

/**
 * Cycles forwards to the next background image.
 */
function nextBackground() {
  changeBackground(1);
}

let currentBackgroundIndex = 0;

// List of background images and their color themes
const backgroundImgs = [
  {
    img: 'forest.jpg',
    headingColor: '#523029',
    headingBgColor: 'rgba(160,82,45,.1)',
  }, {
    img: 'ocean.jpg',
    headingColor: 'navy',
    headingBgColor: 'rgba(65,105,225,.1)',
  }, {
    img: 'mountain.jpg',
    headingColor: 'midnightblue',
    headingBgColor: 'rgba(0,128,128,.15)',
  }, {
    img: 'balloons.jpg',
    headingColor: 'indianred',
    headingBgColor: 'rgba(255,215,0,.2)',
  }, {
    img: 'iceberg.jpg',
    headingColor: 'darkslategray',
    headingBgColor: 'rgba(72,61,139,.2)',
  }, {
    img: 'grass.jpg',
    headingColor: 'green',
    headingBgColor: 'rgba(173,255,47,.2)',
  },
];

/**
 * Advances the background image by a given offset from the current image.
 */
function changeBackground(offset) {
  // Update the index of the current background image
  currentBackgroundIndex = (currentBackgroundIndex + offset) % backgroundImgs.length;

  // Wrap around to the last image if the index is negative
  if (currentBackgroundIndex < 0) {
    currentBackgroundIndex += backgroundImgs.length;
  }

  const newBackground = backgroundImgs[currentBackgroundIndex];

  // Update the page with the new background image
  const html = document.documentElement;
  html.style.backgroundImage = `url(images/${newBackground.img})`;

  // Update the headings to the color theme for the new image
  updateColorTheme(newBackground.headingColor, newBackground.headingBgColor);
}

/**
 * Updates the heading color CSS properties.
 */
function updateColorTheme(headingColor, headingBgColor) {
  document.body.style.setProperty('--heading-color', headingColor);
  document.body.style.setProperty('--heading-bg-color', headingBgColor);
}

/**
 * Fetches a list of messages from the server and adds them to the DOM.
 */
function getMessages() {
  fetch('/data').then(response => response.json()).then((messages) => {
    const messagesElement = document.getElementById('message-container');
    messagesElement.innerHTML = '';

    // Create an <li> element for each message
    for (const message of messages) {
      messagesElement.appendChild(createListElement(message));
    }
  });
}

/** 
  * Creates an <li> element containing text. 
  */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}
